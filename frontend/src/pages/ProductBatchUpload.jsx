import React, { useState, useRef } from 'react';
import { uploadCsvFile, getJobStatus, downloadTemplate } from '../services/batch';
import './ProductBatchUpload.css';

const ProductBatchUpload = () => {
    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [jobExecutionId, setJobExecutionId] = useState(null);
    const [jobStatus, setJobStatus] = useState(null);
    const [error, setError] = useState(null);
    const [dragActive, setDragActive] = useState(false);
    const fileInputRef = useRef(null);

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        validateAndSetFile(selectedFile);
    };

    const validateAndSetFile = (selectedFile) => {
        setError(null);

        if (!selectedFile) {
            return;
        }

        // Validate file type
        if (!selectedFile.name.toLowerCase().endsWith('.csv')) {
            setError('Please select a CSV file');
            return;
        }

        // Validate file size (10MB)
        if (selectedFile.size > 10 * 1024 * 1024) {
            setError('File size must be less than 10MB');
            return;
        }

        setFile(selectedFile);
    };

    const handleDrag = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setDragActive(true);
        } else if (e.type === 'dragleave') {
            setDragActive(false);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            validateAndSetFile(e.dataTransfer.files[0]);
        }
    };

    const handleUpload = async () => {
        if (!file) {
            setError('Please select a file');
            return;
        }

        setUploading(true);
        setError(null);
        setJobStatus(null);

        try {
            const response = await uploadCsvFile(file);
            setJobExecutionId(response.jobExecutionId);
            setJobStatus(response);

            // Start polling for job status
            pollJobStatus(response.jobExecutionId);
        } catch (err) {
            setError(err.response?.data?.error || 'Upload failed. Please try again.');
            setUploading(false);
        }
    };

    const pollJobStatus = async (execId) => {
        const pollInterval = setInterval(async () => {
            try {
                const status = await getJobStatus(execId);
                setJobStatus(status);

                // Stop polling if job is complete or failed
                if (status.status === 'COMPLETED' || status.status === 'FAILED' || status.status === 'STOPPED') {
                    clearInterval(pollInterval);
                    setUploading(false);
                }
            } catch (err) {
                console.error('Error polling job status:', err);
                clearInterval(pollInterval);
                setUploading(false);
            }
        }, 2000); // Poll every 2 seconds
    };

    const handleDownloadTemplate = async () => {
        try {
            await downloadTemplate();
        } catch (err) {
            setError('Failed to download template');
        }
    };

    const handleReset = () => {
        setFile(null);
        setJobExecutionId(null);
        setJobStatus(null);
        setError(null);
        setUploading(false);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    return (
        <div className="batch-upload-container">
            <div className="batch-upload-header">
                <h1>Batch Product Upload</h1>
                <p>Upload a CSV file to import multiple products at once</p>
            </div>

            <div className="batch-upload-content">
                {/* Template Download Section */}
                <div className="template-section">
                    <h3>Need a template?</h3>
                    <p>Download our CSV template with sample data to get started</p>
                    <button onClick={handleDownloadTemplate} className="btn-template">
                        Download CSV Template
                    </button>
                </div>

                {/* File Upload Section */}
                <div className="upload-section">
                    <div
                        className={`file-drop-zone ${dragActive ? 'active' : ''}`}
                        onDragEnter={handleDrag}
                        onDragLeave={handleDrag}
                        onDragOver={handleDrag}
                        onDrop={handleDrop}
                        onClick={() => fileInputRef.current?.click()}
                    >
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".csv"
                            onChange={handleFileChange}
                            style={{ display: 'none' }}
                        />
                        <div className="drop-zone-content">
                            <svg className="upload-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                            </svg>
                            {file ? (
                                <div>
                                    <p className="file-name">{file.name}</p>
                                    <p className="file-size">{(file.size / 1024).toFixed(2)} KB</p>
                                </div>
                            ) : (
                                <div>
                                    <p>Drag and drop your CSV file here, or click to browse</p>
                                    <p className="file-hint">Maximum file size: 10MB</p>
                                </div>
                            )}
                        </div>
                    </div>

                    {file && !uploading && !jobStatus && (
                        <div className="upload-actions">
                            <button onClick={handleUpload} className="btn-upload">
                                Upload and Process
                            </button>
                            <button onClick={handleReset} className="btn-cancel">
                                Cancel
                            </button>
                        </div>
                    )}
                </div>

                {/* Error Display */}
                {error && (
                    <div className="error-message">
                        <svg className="error-icon" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                        </svg>
                        <span>{error}</span>
                    </div>
                )}

                {/* Job Status Display */}
                {jobStatus && (
                    <div className="job-status">
                        <h3>Processing Status</h3>
                        <div className="status-grid">
                            <div className="status-item">
                                <span className="status-label">Status:</span>
                                <span className={`status-value status-${jobStatus.status.toLowerCase()}`}>
                                    {jobStatus.status}
                                </span>
                            </div>
                            {jobStatus.totalRecords !== null && (
                                <>
                                    <div className="status-item">
                                        <span className="status-label">Total Records:</span>
                                        <span className="status-value">{jobStatus.totalRecords}</span>
                                    </div>
                                    <div className="status-item">
                                        <span className="status-label">Successfully Imported:</span>
                                        <span className="status-value success">{jobStatus.successCount}</span>
                                    </div>
                                    <div className="status-item">
                                        <span className="status-label">Failed:</span>
                                        <span className="status-value error">{jobStatus.failureCount}</span>
                                    </div>
                                    {jobStatus.skipCount > 0 && (
                                        <div className="status-item">
                                            <span className="status-label">Skipped:</span>
                                            <span className="status-value warning">{jobStatus.skipCount}</span>
                                        </div>
                                    )}
                                </>
                            )}
                        </div>

                        {jobStatus.message && (
                            <div className="status-message">
                                <p>{jobStatus.message}</p>
                            </div>
                        )}

                        {jobStatus.errors && jobStatus.errors.length > 0 && (
                            <div className="status-errors">
                                <h4>Errors:</h4>
                                <ul>
                                    {jobStatus.errors.map((err, index) => (
                                        <li key={index}>{err}</li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {(jobStatus.status === 'COMPLETED' || jobStatus.status === 'FAILED') && (
                            <button onClick={handleReset} className="btn-new-upload">
                                Upload Another File
                            </button>
                        )}
                    </div>
                )}

                {/* Loading Indicator */}
                {uploading && (
                    <div className="loading-indicator">
                        <div className="spinner"></div>
                        <p>Processing your file... This may take a few moments.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProductBatchUpload;
