const http = require('http');

const BASE_URL = 'http://localhost:8080';

// Test users
const TEST_USERS = [
    { phone: '13800000001', password: '123456', name: 'Admin', role: 'admin' },
    { phone: '13800000004', password: '123456', name: 'Resident', role: 'resident' },
    { phone: '13800000005', password: '123456', name: 'Doctor', role: 'doctor' }
];

let tokens = {
    admin: null,
    resident: null,
    doctor: null
};

let totalTests = 0;
let passedTests = 0;
const results = [];

// Helper function to make HTTP requests
function httpRequest(method, path, body = null, token = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(path, BASE_URL);
        const options = {
            hostname: url.hostname,
            port: url.port || 8080,
            path: url.pathname + url.search,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (token) {
            options.headers['Authorization'] = `Bearer ${token}`;
        }

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                resolve({
                    statusCode: res.statusCode,
                    headers: res.headers,
                    body: data
                });
            });
        });

        req.on('error', (e) => {
            reject(e);
        });

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

// Login function
async function login(phone, password) {
    try {
        const response = await httpRequest('POST', '/api/auth/login', { phone, password });
        if (response.statusCode === 200) {
            const data = JSON.parse(response.body);
            return data.token;
        }
        return null;
    } catch (error) {
        console.error(`Login error: ${error.message}`);
        return null;
    }
}

// Test runner function
async function runTest(testName, method, path, expectedStatus, body = null, token = null, description = '') {
    totalTests++;
    try {
        const response = await httpRequest(method, path, body, token);
        const passed = response.statusCode === expectedStatus;
        
        if (passed) {
            passedTests++;
        }

        const result = {
            testName,
            method,
            path,
            expectedStatus,
            actualStatus: response.statusCode,
            passed,
            response: response.body,
            description
        };
        results.push(result);

        // Print result
        const status = passed ? '✓ PASS' : '✗ FAIL';
        const color = passed ? '\x1b[32m' : '\x1b[31m';
        const reset = '\x1b[0m';
        
        console.log(`\n${color}${status}${reset} - ${testName}`);
        console.log(`Request: ${method} ${path}`);
        console.log(`Expected Status: ${expectedStatus}, Actual: ${response.statusCode}`);
        if (!passed && response.body) {
            console.log(`Response: ${response.body.substring(0, 200)}`);
        }

        return result;
    } catch (error) {
        const result = {
            testName,
            method,
            path,
            expectedStatus,
            actualStatus: 0,
            passed: false,
            error: error.message,
            description
        };
        results.push(result);
        
        console.log(`\n\x1b[31m✗ FAIL\x1b[0m - ${testName}`);
        console.log(`Error: ${error.message}`);
        
        return result;
    }
}

// Main test execution
async function runAllTests() {
    console.log('\x1b[35m========================================\x1b[0m');
    console.log('\x1b[35m  Health Module Testing Started\x1b[0m');
    console.log('\x1b[35m  Base URL: http://localhost:8080\x1b[0m');
    console.log('\x1b[35m========================================\x1b[0m\n');

    // Step 1: Login all test users
    console.log('\x1b[36m========== Step 1: User Authentication ==========\x1b[0m');
    for (const user of TEST_USERS) {
        const token = await login(user.phone, user.password);
        if (token) {
            tokens[user.role] = token;
            console.log(`\x1b[32m✓ ${user.name} login successful, token obtained\x1b[0m`);
        } else {
            console.log(`\x1b[31m✗ ${user.name} login failed\x1b[0m`);
        }
    }

    console.log('\n');

    // ========== 1. Health Records Management ==========
    console.log('\x1b[36m========== 1. Health Records Management ==========\x1b[0m');
    
    await runTest(
        'Resident Get Health Records',
        'GET',
        '/api/health/records',
        200,
        null,
        tokens.resident,
        'Test resident viewing their health records'
    );

    await runTest(
        'Resident Get Health Stats',
        'GET',
        '/api/health/stats',
        200,
        null,
        tokens.resident,
        'Test resident viewing their health statistics'
    );

    // ========== 2. Doctor Health Management ==========
    console.log('\n\x1b[36m========== 2. Doctor Health Management ==========\x1b[0m');

    await runTest(
        'Doctor Query Resident List',
        'GET',
        '/api/doctor-residents/list',
        200,
        null,
        tokens.doctor,
        'Test doctor querying resident list'
    );

    await runTest(
        'Doctor Record Health Check',
        'POST',
        '/api/doctor-residents/health-check',
        200,
        {
            userId: 4,
            checkDate: '2026-03-07',
            height: 175,
            weight: 70,
            bloodPressure: '120/80',
            heartRate: 75,
            notes: 'Good health condition'
        },
        tokens.doctor,
        'Test doctor recording health check data'
    );

    await runTest(
        'Doctor Update Resident Tags',
        'POST',
        '/api/doctor-residents/tags/4',
        200,
        { tags: ['hypertension', 'diabetes'] },
        tokens.doctor,
        'Test doctor updating resident health tags'
    );

    await runTest(
        'Doctor Get Resident Health Records',
        'GET',
        '/api/doctor-residents/health-records/4',
        200,
        null,
        tokens.doctor,
        'Test doctor getting resident health records'
    );

    // ========== 3. Appointment Management ==========
    console.log('\n\x1b[36m========== 3. Appointment Management ==========\x1b[0m');

    await runTest(
        'Resident Get Doctor List',
        'GET',
        '/api/appointment/doctors',
        200,
        null,
        tokens.resident,
        'Test resident getting doctor list'
    );

    await runTest(
        'Resident Get Doctor Schedule',
        'GET',
        '/api/appointment/schedule/1',
        200,
        null,
        tokens.resident,
        'Test resident getting doctor schedule'
    );

    await runTest(
        'Resident Create Appointment',
        'POST',
        '/api/appointment/create',
        200,
        {
            doctorId: 1,
            scheduleId: 1,
            patientName: 'ZhangSan',
            patientPhone: '13800138000',
            reason: 'Regular health checkup'
        },
        tokens.resident,
        'Test resident creating an appointment'
    );

    await runTest(
        'Doctor Confirm Appointment',
        'POST',
        '/api/appointment/confirm/1',
        200,
        null,
        tokens.doctor,
        'Test doctor confirming an appointment'
    );

    await runTest(
        'Doctor Complete Appointment',
        'POST',
        '/api/appointment/complete/1',
        200,
        null,
        tokens.doctor,
        'Test doctor completing an appointment'
    );

    await runTest(
        'Resident Cancel Appointment',
        'POST',
        '/api/appointment/cancel/1',
        200,
        null,
        tokens.resident,
        'Test resident cancelling an appointment'
    );

    await runTest(
        'Resident Get Appointment List',
        'GET',
        '/api/appointment/list',
        200,
        null,
        tokens.resident,
        'Test resident getting their appointment list'
    );

    // ========== 4. Vaccine Appointment ==========
    console.log('\n\x1b[36m========== 4. Vaccine Appointment ==========\x1b[0m');

    await runTest(
        'Get Vaccine Appointment List',
        'GET',
        '/api/appointment/vaccine/list',
        200,
        null,
        tokens.resident,
        'Test getting vaccine appointment list'
    );

    // ========== 5. Chronic Disease & Health Education ==========
    console.log('\n\x1b[36m========== 5. Chronic Disease & Health Education ==========\x1b[0m');

    await runTest(
        'Create Health Reminder',
        'POST',
        '/api/health/reminder',
        200,
        {
            userId: 4,
            reminderType: 'medication',
            reminderTime: '08:00',
            content: 'Please take blood pressure medicine on time',
            frequency: 'daily'
        },
        tokens.doctor,
        'Test doctor creating health reminder for resident'
    );

    await runTest(
        'Get My Health Reminders',
        'GET',
        '/api/health/reminder/my',
        200,
        null,
        tokens.resident,
        'Test resident getting their health reminders'
    );

    await runTest(
        'Get Doctor Health Reminders List',
        'GET',
        '/api/health/reminder/doctor/list',
        200,
        null,
        tokens.doctor,
        'Test doctor getting their published health reminders'
    );

    await runTest(
        'Publish Health Education',
        'POST',
        '/api/health/education',
        200,
        {
            title: 'Spring Flu Prevention Guide',
            content: 'Spring is the flu season, please pay attention to prevention...',
            category: 'prevention',
            publishDate: '2026-03-07'
        },
        tokens.doctor,
        'Test doctor publishing health education material'
    );

    await runTest(
        'Get Health Education List',
        'GET',
        '/api/health/education/list',
        200,
        null,
        tokens.resident,
        'Test resident getting health education list'
    );

    await runTest(
        'Get Doctor Health Education List',
        'GET',
        '/api/health/education/doctor/list',
        200,
        null,
        tokens.doctor,
        'Test doctor getting their published education list'
    );

    // ========== 6. Doctor Schedule Management ==========
    console.log('\n\x1b[36m========== 6. Doctor Schedule Management ==========\x1b[0m');

    await runTest(
        'Create Doctor Schedule',
        'POST',
        '/api/schedule/create',
        200,
        {
            doctorId: 5,
            date: '2026-03-08',
            timeSlot: 'morning',
            maxAppointments: 20,
            location: 'Clinic 3F'
        },
        tokens.doctor,
        'Test doctor creating their schedule'
    );

    await runTest(
        'Get Schedule List',
        'GET',
        '/api/schedule/list?doctorId=5',
        200,
        null,
        tokens.resident,
        'Test resident getting doctor schedule list'
    );

    await runTest(
        'Delete Schedule',
        'DELETE',
        '/api/schedule/delete/1',
        200,
        null,
        tokens.doctor,
        'Test doctor deleting a schedule'
    );

    // ========== Test Summary ==========
    console.log('\n\x1b[35m========================================\x1b[0m');
    console.log('\x1b[35m  Test Summary\x1b[0m');
    console.log('\x1b[35m========================================\x1b[0m\n');

    const failedTests = totalTests - passedTests;
    const passRate = ((passedTests / totalTests) * 100).toFixed(2);

    console.log(`Total Tests: \x1b[36m${totalTests}\x1b[0m`);
    console.log(`Passed: \x1b[32m${passedTests}\x1b[0m`);
    console.log(`Failed: \x1b[31m${failedTests}\x1b[0m`);
    console.log(`Pass Rate: \x1b[33m${passRate}%\x1b[0m`);

    console.log('\n\x1b[36mDetailed Results:\x1b[0m');
    results.forEach((result, index) => {
        const status = result.passed ? '\x1b[32m[PASS]\x1b[0m' : '\x1b[31m[FAIL]\x1b[0m';
        const errorMsg = result.error ? ` - ${result.error}` : '';
        console.log(`${status} ${result.testName} - Status: ${result.actualStatus}${errorMsg}`);
    });

    console.log('\n\x1b[35m========================================\x1b[0m\n');

    // Generate report
    generateReport(passRate, failedTests);
}

// Generate test report
function generateReport(passRate, failedTests) {
    const report = `
# Health Module Test Report

## Test Overview

**Test Date**: ${new Date().toISOString().split('T')[0]}  
**Test Time**: ${new Date().toLocaleTimeString()}  
**Base URL**: http://localhost:8080  
**Total Tests**: ${totalTests}  
**Passed**: ${passedTests}  
**Failed**: ${failedTests}  
**Pass Rate**: ${passRate}%  

## Test Results Summary

${results.map((r, i) => `${i + 1}. [${r.passed ? 'PASS' : 'FAIL'}] ${r.testName} - ${r.method} ${r.path} (Expected: ${r.expectedStatus}, Actual: ${r.actualStatus})`).join('\n')}

## Recommendations

${failedTests === 0 ? 'All tests passed! The health module is functioning correctly.' : 'Some tests failed. Please review the failed test cases and fix the issues.'}
`;

    const fs = require('fs');
    fs.writeFileSync('health-module-test-report.md', report);
    console.log('\nTest report saved to: health-module-test-report.md\n');
}

// Run all tests
runAllTests().catch(console.error);
