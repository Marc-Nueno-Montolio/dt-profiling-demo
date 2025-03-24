<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Diagnostic Tools</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;500;600;700&display=swap">
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <!-- Dynatrace-style header -->
    <header class="dt-header">
        <div class="dt-header-content">
            <div class="dt-logo">
                <span class="dt-product-name">Diagnostic Tools</span>
            </div>
        </div>
    </header>
    
    <div class="dt-main-container">
        <div class="dt-page-header">
            <h1>Diagnostic Tools</h1>
            <p class="dt-subtitle">Use this control panel to generate various performance issues for troubleshooting purposes</p>
        </div>

        <div class="dt-tabs">
            <button class="dt-tab-button active" data-tab="exceptions">Exceptions</button>
            <button class="dt-tab-button" data-tab="memory">Memory</button>
            <button class="dt-tab-button" data-tab="cpu">CPU</button>
            <button class="dt-tab-button" data-tab="database">Database</button>
            <button class="dt-tab-button" data-tab="gc">Garbage Collection</button>
            <button class="dt-tab-button" data-tab="jmx">JMX Metrics</button>
            <button class="dt-tab-button" data-tab="logs">Logs</button>
            <a href="/manager/html" target="_blank" class="dt-tab-button manager-link">Tomcat Manager</a>
        </div>

        <div class="dt-tab-content">
            <!-- Exception Generator Panel -->
            <div class="dt-panel" id="exceptions-panel">
                <div class="dt-panel-header">
                    <h2>Exception Generator</h2>
                </div>
                <div class="dt-panel-content">
                    <form id="exceptionForm">
                        <div class="dt-form-group">
                            <label for="type">Type:</label>
                            <select id="type" name="type" class="dt-select">
                                <option value="nullPointer">Null Pointer Exception</option>
                                <option value="arithmetic">Arithmetic Exception</option>
                                <option value="outOfMemory">Out of Memory Error</option>
                                <option value="stackOverflow">Stack Overflow Error</option>
                            </select>
                        </div>
                        <div class="dt-button-group">
                            <button type="submit" class="dt-button primary">Generate Exception</button>
                        </div>
                    </form>
                    <div id="exceptionResult" class="dt-result"></div>
                </div>
            </div>

            <!-- Memory Panel -->
            <div class="dt-panel" id="memory-panel">
                <div class="dt-panel-header">
                    <h2>Memory Issue Generator</h2>
                </div>
                <div class="dt-panel-content">
                    <form id="memoryForm">
                        <div class="dt-form-group">
                            <label for="duration">Duration (seconds):</label>
                            <input type="number" id="duration" name="duration" value="30" min="1" max="300" class="dt-input">
                        </div>
                        <div class="dt-button-group">
                            <button type="submit" class="dt-button primary">Generate Memory Leak</button>
                        </div>
                    </form>
                    <div id="memoryResult" class="dt-result"></div>
                </div>
            </div>

            <!-- CPU Panel -->
            <div class="dt-panel" id="cpu-panel">
                <div class="dt-panel-header">
                    <h2>CPU Load Generator</h2>
                </div>
                <div class="dt-panel-content">
                    <form id="cpuForm">
                        <div class="dt-form-group">
                            <label for="duration">Duration (seconds):</label>
                            <input type="number" id="duration" name="duration" value="30" min="1" max="300" class="dt-input">
                        </div>
                        <div class="dt-button-group">
                            <button type="submit" class="dt-button primary">Generate High CPU</button>
                        </div>
                    </form>
                    <div id="cpuResult" class="dt-result"></div>
                </div>
            </div>

            <!-- Database Panel -->
            <div class="dt-panel" id="database-panel">
                <div class="dt-panel-header">
                    <h2>Database Performance Issues</h2>
                </div>
                <div class="dt-panel-content">
                    <form id="databaseForm">
                        <div class="dt-form-group">
                            <label for="dbIssueType">Issue Type:</label>
                            <select id="dbIssueType" name="type" class="dt-select">
                                <option value="slow_query">Slow Query</option>
                                <option value="connection_leak">Connection Leak</option>
                                <option value="deadlock">Database Deadlock</option>
                                <option value="full_table_scan">Full Table Scan</option>
                            </select>
                        </div>
                        <div class="dt-form-group">
                            <label for="dbDuration">Duration (seconds):</label>
                            <input type="number" id="dbDuration" name="duration" value="30" min="1" max="300" class="dt-input">
                        </div>
                        <div class="dt-form-group">
                            <label for="dbConnections">Number of Connections:</label>
                            <input type="number" id="dbConnections" name="connections" value="10" min="1" max="100" class="dt-input">
                        </div>
                        <div class="dt-button-group">
                            <button type="submit" class="dt-button primary">Generate Database Issue</button>
                        </div>
                    </form>
                    <div id="databaseResult" class="dt-result"></div>
                </div>
            </div>

            <!-- Garbage Collection Panel -->
            <div class="dt-panel" id="gc-panel">
                <div class="dt-panel-header">
                    <h2>Garbage Collection Issues</h2>
                </div>
                <div class="dt-panel-content">
                    <form id="gcForm">
                        <div class="dt-form-group">
                            <label for="gcType">Type:</label>
                            <select id="gcType" name="type" class="dt-select">
                                <option value="frequent_gc">Frequent GC</option>
                                <option value="long_gc">Long GC Pause</option>
                                <option value="concurrent_gc">Concurrent Mode Failure</option>
                            </select>
                        </div>
                        <div class="dt-form-group">
                            <label for="gcDuration">Duration (seconds):</label>
                            <input type="number" id="gcDuration" name="duration" value="30" min="1" max="300" class="dt-input">
                        </div>
                        <div class="dt-button-group">
                            <button type="submit" class="dt-button primary">Generate GC Issue</button>
                        </div>
                    </form>
                    <div id="gcResult" class="dt-result"></div>
                </div>
            </div>

            <!-- JMX Metrics Panel -->
            <div class="dt-panel" id="jmx-panel">
                <div class="dt-panel-header">
                    <h2>JMX Metrics</h2>
                </div>
                <div class="dt-panel-content">
                    <div class="metrics-container">
                        <div class="metric-group">
                            <h3>Memory</h3>
                            <div id="heapMemory"></div>
                            <div id="nonHeapMemory"></div>
                        </div>
                        <div class="metric-group">
                            <h3>Threads</h3>
                            <div id="threadCount"></div>
                            <div id="deadlockedThreads"></div>
                        </div>
                        <div class="metric-group">
                            <h3>Classes</h3>
                            <div id="loadedClasses"></div>
                            <div id="unloadedClasses"></div>
                        </div>
                        <div class="metric-group">
                            <h3>Operating System</h3>
                            <div id="systemLoad"></div>
                            <div id="processCpuTime"></div>
                        </div>
                    </div>
                    <button id="refreshMetrics" class="dt-button primary">Refresh Metrics</button>
                </div>
            </div>

            <!-- Enhanced Logs Panel -->
            <div class="dt-panel" id="logs-panel">
                <div class="dt-panel-header">
                    <h2>Application Logs</h2>
                </div>
                <div class="dt-panel-content">
                    <div class="dt-form-group">
                        <label for="logFile">Log File:</label>
                        <select id="logFile" name="logFile" class="dt-select">
                            <option value="catalina.out">catalina.out (Tomcat)</option>
                            <option value="localhost.log">localhost.log</option>
                            <option value="manager.log">manager.log</option>
                            <option value="host-manager.log">host-manager.log</option>
                        </select>
                    </div>
                    <div class="dt-form-group">
                        <label for="logLines">Lines to show:</label>
                        <input type="number" id="logLines" name="lines" value="100" min="10" max="1000" class="dt-input">
                    </div>
                    <div class="dt-form-group">
                        <label>
                            <input type="checkbox" id="autoRefresh" checked> Auto-refresh (5s)
                        </label>
                        <label>
                            <input type="checkbox" id="followTail" checked> Follow tail
                        </label>
                    </div>
                    <div class="dt-button-group">
                        <button onclick="refreshLogs()" class="dt-button primary">Refresh Logs</button>
                        <button onclick="clearLogs()" class="dt-button secondary">Clear Logs</button>
                        <button onclick="downloadLogs()" class="dt-button secondary">Download Logs</button>
                    </div>
                    <div class="dt-log-container">
                        <pre id="logContent" class="dt-log-content"></pre>
                        <div class="dt-log-status">
                            <span id="logStatus">Loading logs...</span>
                            <span id="logSize"></span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <footer class="dt-footer">
        <p>Developed by Marc Nueno: Sr. ACE Services Delivery Consultant</p>
    </footer>

    <script src="js/scripts.js"></script>
</body>
</html> 