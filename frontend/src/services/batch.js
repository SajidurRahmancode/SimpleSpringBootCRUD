import api from './api';

/**
 * Upload a CSV file for batch product import
 * @param {File} file - The CSV file to upload
 * @returns {Promise} Response with job execution details
 */
export const uploadCsvFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post('/api/products/batch/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });

    return response.data;
};

/**
 * Get the status of a batch job execution
 * @param {number} jobExecutionId - The job execution ID
 * @returns {Promise} Job status and statistics
 */
export const getJobStatus = async (jobExecutionId) => {
    const response = await api.get(`/api/products/batch/status/${jobExecutionId}`);
    return response.data;
};

/**
 * Download the CSV template file
 * @returns {Promise} Blob containing the CSV template
 */
export const downloadTemplate = async () => {
    const response = await api.get('/api/products/batch/template', {
        responseType: 'blob',
    });

    // Create a download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'product_template.csv');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
};
