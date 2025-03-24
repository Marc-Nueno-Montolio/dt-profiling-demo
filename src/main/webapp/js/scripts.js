// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    // Set up event listeners for form changes
    const exceptionType = document.getElementById('exceptionType');
    if (exceptionType) {
        exceptionType.addEventListener('change', function() {
            const depthGroup = document.getElementById('depthGroup');
            if (depthGroup) {
                depthGroup.style.display = this.value === 'deep' ? 'block' : 'none';
            }
        });
    }
    
    const dbQueryType = document.getElementById('dbQueryType');
    if (dbQueryType) {
        dbQueryType.addEventListener('change', function() {
            const iterationsGroup = document.getElementById('iterationsGroup');
            const connectionCountGroup = document.getElementById('connectionCountGroup');
            if (iterationsGroup && connectionCountGroup) {
                iterationsGroup.style.display = this.value === 'query' ? 'block' : 'none';
                connectionCountGroup.style.display = this.value === 'connections' ? 'block' : 'none';
            }
        });
    }

    // Tab switching
    const tabs = document.querySelectorAll('.dt-tab-button');
    const panels = document.querySelectorAll('.dt-panel');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Skip if it's the Tomcat Manager link
            if (tab.classList.contains('manager-link')) return;
            
            // Remove active class from all tabs and panels
            tabs.forEach(t => t.classList.remove('active'));
            panels.forEach(p => p.style.display = 'none');
            
            // Add active class to clicked tab
            tab.classList.add('active');
            
            // Show corresponding panel
            const panelId = tab.getAttribute('data-tab');
            const panel = document.getElementById(panelId + '-panel');
            if (panel) {
                panel.style.display = 'block';
            }
        });
    });

    // Set first tab as active by default
    const firstTab = document.querySelector('.dt-tab-button:not(.manager-link)');
    if (firstTab) {
        firstTab.click();
    }

    // Exception Form
    const exceptionForm = document.getElementById('exceptionForm');
    if (exceptionForm) {
        exceptionForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(exceptionForm);
            try {
                const response = await fetch('/exception', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams(formData).toString()
                });
                const text = await response.text();
                let result;
                try {
                    result = JSON.parse(text);
                } catch (parseError) {
                    result = { status: 'error', message: text };
                }
                let resultHtml = `
                    <div class="dt-result ${result.status === 'success' ? 'success' : 'error'}">
                        <h3>${result.message || 'Unknown error'}</h3>
                        <div class="exception-details">
                            <h4>Exception Type:</h4>
                            <pre>${result.exceptionType || 'Unknown'}</pre>
                            <h4>Message:</h4>
                            <pre>${result.exceptionMessage || 'No message available'}</pre>
                            <h4>Stack Trace:</h4>
                            <pre>${result.stackTrace ? result.stackTrace.replace(/\\n/g, '\n') : 'No stack trace available'}</pre>
                        </div>
                    </div>`;
                document.getElementById('exceptionResult').innerHTML = resultHtml;
            } catch (error) {
                document.getElementById('exceptionResult').innerHTML = `
                    <div class="dt-result error">
                        <h3>Error</h3>
                        <div class="exception-details">
                            <h4>Message:</h4>
                            <pre>${error.message}</pre>
                        </div>
                    </div>`;
            }
        });
    }

    // Memory Form
    const memoryForm = document.getElementById('memoryForm');
    if (memoryForm) {
        memoryForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(memoryForm);
            formData.append('type', 'memory');
            try {
                const response = await fetch('/performance', {
                    method: 'POST',
                    body: new URLSearchParams(formData)
                });
                const result = await response.text();
                document.getElementById('memoryResult').textContent = result;
            } catch (error) {
                document.getElementById('memoryResult').textContent = 'Error: ' + error.message;
            }
        });
    }

    // CPU Form
    const cpuForm = document.getElementById('cpuForm');
    if (cpuForm) {
        cpuForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(cpuForm);
            formData.append('type', 'cpu');
            try {
                const response = await fetch('/performance', {
                    method: 'POST',
                    body: new URLSearchParams(formData)
                });
                const result = await response.text();
                document.getElementById('cpuResult').textContent = result;
            } catch (error) {
                document.getElementById('cpuResult').textContent = 'Error: ' + error.message;
            }
        });
    }

    // Database Form
    const databaseForm = document.getElementById('databaseForm');
    if (databaseForm) {
        databaseForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(databaseForm);
            try {
                const response = await fetch('/database', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams(formData).toString()
                });
                const text = await response.text();
                let result;
                try {
                    result = JSON.parse(text);
                } catch (parseError) {
                    result = { status: 'success', message: text };
                }
                document.getElementById('databaseResult').innerHTML = `
                    <div class="dt-result ${result.status === 'success' ? 'success' : 'error'}">
                        <h3>${result.message}</h3>
                    </div>`;
            } catch (error) {
                document.getElementById('databaseResult').innerHTML = `
                    <div class="dt-result error">
                        <h3>Error</h3>
                        <pre>${error.message}</pre>
                    </div>`;
            }
        });
    }

    // GC Form
    const gcForm = document.getElementById('gcForm');
    if (gcForm) {
        gcForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(gcForm);
            try {
                const response = await fetch('/metrics', {
                    method: 'POST',
                    body: new URLSearchParams(formData)
                });
                const result = await response.text();
                document.getElementById('gcResult').textContent = result;
            } catch (error) {
                document.getElementById('gcResult').textContent = 'Error: ' + error.message;
            }
        });
    }

    // JMX Metrics
    async function updateMetrics() {
        try {
            const response = await fetch('/metrics');
            const metrics = await response.json();
            
            // Update Memory metrics
            const heapUsed = formatBytes(metrics.heap.used);
            const heapMax = formatBytes(metrics.heap.max);
            const nonHeapUsed = formatBytes(metrics.nonHeap.used);
            
            document.getElementById('heapMemory').textContent = `Heap Memory: ${heapUsed} / ${heapMax}`;
            document.getElementById('nonHeapMemory').textContent = `Non-Heap Memory: ${nonHeapUsed}`;
            
            // Update Thread metrics
            document.getElementById('threadCount').textContent = 
                `Live Threads: ${metrics.threads.count} (Peak: ${metrics.threads.peakCount})`;
            document.getElementById('deadlockedThreads').textContent = 
                `Deadlocked Threads: ${metrics.threads.deadlockedCount}`;
            
            // Update Class metrics
            document.getElementById('loadedClasses').textContent = 
                `Loaded Classes: ${metrics.classes.loaded}`;
            document.getElementById('unloadedClasses').textContent = 
                `Total Unloaded: ${metrics.classes.unloaded}`;
            
            // Update OS metrics
            document.getElementById('systemLoad').textContent = 
                `System Load: ${metrics.os.systemLoad.toFixed(2)}`;
            document.getElementById('processCpuTime').textContent = 
                `Available Processors: ${metrics.os.processors}`;
        } catch (error) {
            console.error('Error updating metrics:', error);
        }
    }

    const refreshMetricsButton = document.getElementById('refreshMetrics');
    if (refreshMetricsButton) {
        refreshMetricsButton.addEventListener('click', updateMetrics);
        updateMetrics(); // Initial update
        setInterval(updateMetrics, 5000); // Auto-update every 5 seconds
    }

    // Logs functionality
    let logRefreshInterval;
    const logFileSelect = document.getElementById('logFile');
    const logLines = document.getElementById('logLines');
    const autoRefresh = document.getElementById('autoRefresh');
    const followTail = document.getElementById('followTail');
    const logContent = document.getElementById('logContent');
    const logInfo = document.getElementById('logInfo');

    async function updateLogFileList() {
        try {
            const response = await fetch('/logs?action=list');
            const files = await response.json();
            logFileSelect.innerHTML = '';
            files.forEach(file => {
                const option = document.createElement('option');
                option.value = file.name;
                option.textContent = file.name;
                logFileSelect.appendChild(option);
            });
            if (files.length > 0) {
                await refreshLogs();
            }
        } catch (error) {
            console.error('Error updating log file list:', error);
        }
    }

    async function refreshLogs() {
        const selectedFile = logFileSelect.value;
        const numLines = logLines.value;
        
        try {
            const response = await fetch(`/logs?file=${selectedFile}&lines=${numLines}`);
            const content = await response.text();
            logContent.textContent = content;
            
            // Update log info
            const size = response.headers.get('X-Log-Size');
            const lines = response.headers.get('X-Log-Lines');
            const lastModified = new Date(parseInt(response.headers.get('X-Log-LastModified'))).toLocaleString();
            
            logInfo.textContent = `Size: ${formatBytes(size)} | Lines: ${lines} | Last Modified: ${lastModified}`;
            
            if (followTail.checked) {
                logContent.scrollTop = logContent.scrollHeight;
            }
        } catch (error) {
            console.error('Error refreshing logs:', error);
        }
    }

    function formatBytes(bytes) {
        if (!bytes) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Log controls event listeners
    if (logFileSelect) {
        updateLogFileList();
        
        logFileSelect.addEventListener('change', refreshLogs);
        logLines.addEventListener('change', refreshLogs);
        
        document.getElementById('refreshLogs').addEventListener('click', refreshLogs);
        
        document.getElementById('clearLogs').addEventListener('click', async () => {
            try {
                const response = await fetch('/logs', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: `action=clear&file=${logFileSelect.value}`
                });
                await refreshLogs();
            } catch (error) {
                console.error('Error clearing logs:', error);
            }
        });
        
        document.getElementById('downloadLogs').addEventListener('click', () => {
            window.location.href = `/logs?action=download&file=${logFileSelect.value}`;
        });
        
        autoRefresh.addEventListener('change', () => {
            if (autoRefresh.checked) {
                logRefreshInterval = setInterval(refreshLogs, 5000);
            } else {
                clearInterval(logRefreshInterval);
            }
        });
        
        // Initial auto-refresh setup
        if (autoRefresh.checked) {
            logRefreshInterval = setInterval(refreshLogs, 5000);
        }
    }
});

// Helper function to format form data
function getFormDataAsJson(form) {
    const formData = new FormData(form);
    const data = {};
    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }
    return data;
}

// Memory Leak functions
function generateMemoryLeak() {
    const form = document.getElementById('memoryLeakForm');
    const data = getFormDataAsJson(form);
    
    fetch('memory-leak', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams(data).toString()
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('memoryLeakResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('memoryLeakResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

function cleanupMemoryLeak() {
    fetch('memory-leak', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'action=cleanup'
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('memoryLeakResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('memoryLeakResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

// CPU Load functions
function generateCpuLoad() {
    const form = document.getElementById('cpuLoadForm');
    const data = getFormDataAsJson(form);
    
    fetch('cpu-load', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams(data).toString()
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('cpuLoadResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('cpuLoadResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

function stopCpuLoad() {
    fetch('cpu-load', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'action=stop'
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('cpuLoadResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('cpuLoadResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

// Crash functions
function simulateCrash() {
    const form = document.getElementById('crashForm');
    const data = getFormDataAsJson(form);
    
    fetch('crash', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams(data).toString()
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('crashResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('crashResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

function cancelCrash() {
    fetch('crash', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'action=cancel'
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('crashResult').innerHTML = 
            `<div class="${data.status}">${data.message}</div>`;
    })
    .catch(error => {
        document.getElementById('crashResult').innerHTML = 
            `<div class="error">Error: ${error.message}</div>`;
    });
}

// Remove duplicate event listeners
document.getElementById('performanceForm')?.removeEventListener('submit', null);
document.getElementById('cpuForm')?.removeEventListener('submit', null);
document.getElementById('databaseForm')?.removeEventListener('submit', null);

// Remove duplicate functions
const functionsToRemove = [
    'generateException',
    'executeDbOperation',
    'refreshLogs',
    'clearLogs',
    'downloadLogs',
    'formatBytes'
];

functionsToRemove.forEach(func => {
    if (window[func]) {
        delete window[func];
    }
}); 