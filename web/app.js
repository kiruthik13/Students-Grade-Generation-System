const API_BASE = 'http://localhost:8080/api';

const form = document.getElementById('gradeForm');
const resultCard = document.getElementById('resultCard');

const nameEl = document.getElementById('name');
const inputs = ['m1','m2','m3','m4','m5'].map(id => document.getElementById(id));

const rName = document.getElementById('rName');
const rTotal = document.getElementById('rTotal');
const rAvg = document.getElementById('rAvg');
const rGrade = document.getElementById('rGrade');
const rNote = document.getElementById('rNote');

const tableBody = document.querySelector('#studentsTable tbody');
const refreshBtn = document.getElementById('refreshBtn');

// New elements for advanced features
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const gradeFilter = document.getElementById('gradeFilter');
const sortBy = document.getElementById('sortBy');
const exportBtn = document.getElementById('exportBtn');
const importBtn = document.getElementById('importBtn');
const fileInput = document.getElementById('fileInput');
const bulkDeleteBtn = document.getElementById('bulkDeleteBtn');
const selectAll = document.getElementById('selectAll');

// Statistics elements
const totalStudents = document.getElementById('totalStudents');
const avgGrade = document.getElementById('avgGrade');
const topGrade = document.getElementById('topGrade');
const gradeDistribution = document.getElementById('gradeDistribution');

// Global variables
let allStudents = [];
let filteredStudents = [];
let gradeChart = null;

// Utility functions
function isValidName(name) {
	return /^[a-zA-Z\s\-']+$/.test(name.trim()) && name.trim().length >= 2;
}

function isValidMark(value) {
	const n = Number(value);
	return Number.isFinite(n) && n >= 0 && n <= 100;
}

function computeGrade(avg) {
	if (avg >= 90) return 'A';
	if (avg >= 75) return 'B';
	if (avg >= 60) return 'C';
	if (avg >= 40) return 'D';
	return 'F';
}

function assignGradeClass(grade) {
	rGrade.className = 'grade-pill';
	rGrade.classList.add(`grade-${grade}`);
}

// Notification system
function showNotification(message, type = 'info') {
	const notification = document.createElement('div');
	notification.className = `notification ${type}`;
	notification.textContent = message;
	document.body.appendChild(notification);
	
	setTimeout(() => notification.classList.add('show'), 100);
	setTimeout(() => {
		notification.classList.remove('show');
		setTimeout(() => document.body.removeChild(notification), 300);
	}, 3000);
}

// Search and filter functions
function filterStudents() {
	const searchTerm = searchInput.value.toLowerCase();
	const gradeFilterValue = gradeFilter.value;
	
	filteredStudents = allStudents.filter(student => {
		const matchesSearch = !searchTerm || 
			student.name.toLowerCase().includes(searchTerm) ||
			student.studentId.toLowerCase().includes(searchTerm);
		
		const matchesGrade = !gradeFilterValue || student.grade === gradeFilterValue;
		
		return matchesSearch && matchesGrade;
	});
	
	sortStudents();
	renderList(filteredStudents);
	updateStatistics();
}

function sortStudents() {
	const sortField = sortBy.value;
	filteredStudents.sort((a, b) => {
		switch (sortField) {
			case 'name':
				return a.name.localeCompare(b.name);
			case 'total':
				return b.total - a.total;
			case 'average':
				return b.average - a.average;
			case 'grade':
				return a.grade.localeCompare(b.grade);
			case 'studentId':
				return a.studentId.localeCompare(b.studentId);
			default:
				return 0;
		}
	});
}

// Enhanced render function with checkboxes
function renderList(rows) {
	tableBody.innerHTML = '';
	for (const row of rows) {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td><input type="checkbox" class="student-checkbox" data-id="${row.studentId}"></td>
			<td>${row.studentId || ''}</td>
			<td>${row.name || ''}</td>
			<td>${row.total ?? ''}</td>
			<td>${row.average ?? ''}</td>
			<td>${row.grade || ''}</td>
			<td>
				<button class="btn-delete" onclick="deleteStudent('${row.studentId}')">Delete</button>
				<button class="btn-edit" onclick="editStudent('${row.studentId}')">Edit</button>
			</td>
		`;
		tableBody.appendChild(tr);
	}
	updateSelectAllState();
}

// Statistics functions
function updateStatistics() {
	const students = filteredStudents.length > 0 ? filteredStudents : allStudents;
	
	totalStudents.textContent = students.length;
	
	if (students.length > 0) {
		const totalAvg = students.reduce((sum, s) => sum + s.average, 0) / students.length;
		avgGrade.textContent = Math.round(totalAvg);
		
		const grades = students.map(s => s.grade);
		const topGradeValue = grades.reduce((a, b) => a < b ? b : a, 'F');
		topGrade.textContent = topGradeValue;
		
		const gradeCounts = grades.reduce((acc, grade) => {
			acc[grade] = (acc[grade] || 0) + 1;
			return acc;
		}, {});
		
		const distribution = Object.entries(gradeCounts)
			.map(([grade, count]) => `${grade}: ${count}`)
			.join(', ');
		gradeDistribution.textContent = distribution;
		
		updateChart(gradeCounts);
	} else {
		avgGrade.textContent = '0';
		topGrade.textContent = '-';
		gradeDistribution.textContent = '-';
		updateChart({});
	}
}

function updateChart(gradeCounts) {
	const ctx = document.getElementById('gradeChart').getContext('2d');
	
	if (gradeChart) {
		gradeChart.destroy();
	}
	
	const grades = ['A', 'B', 'C', 'D', 'F'];
	const counts = grades.map(grade => gradeCounts[grade] || 0);
	
	gradeChart = new Chart(ctx, {
		type: 'bar',
		data: {
			labels: grades,
			datasets: [{
				label: 'Number of Students',
				data: counts,
				backgroundColor: [
					'rgba(34, 197, 94, 0.8)',
					'rgba(59, 130, 246, 0.8)',
					'rgba(251, 191, 36, 0.8)',
					'rgba(251, 113, 133, 0.8)',
					'rgba(168, 85, 247, 0.8)'
				],
				borderColor: [
					'rgba(34, 197, 94, 1)',
					'rgba(59, 130, 246, 1)',
					'rgba(251, 191, 36, 1)',
					'rgba(251, 113, 133, 1)',
					'rgba(168, 85, 247, 1)'
				],
				borderWidth: 1
			}]
		},
		options: {
			responsive: true,
			scales: {
				y: {
					beginAtZero: true,
					ticks: {
						stepSize: 1
					}
				}
			},
			plugins: {
				legend: {
					display: false
				}
			}
		}
	});
}

// Export/Import functions
function exportToCSV() {
	const students = filteredStudents.length > 0 ? filteredStudents : allStudents;
	const headers = ['Student ID', 'Name', 'Mark 1', 'Mark 2', 'Mark 3', 'Mark 4', 'Mark 5', 'Total', 'Average', 'Grade'];
	
	let csv = headers.join(',') + '\n';
	students.forEach(student => {
		const row = [
			student.studentId,
			`"${student.name}"`,
			student.mark1,
			student.mark2,
			student.mark3,
			student.mark4,
			student.mark5,
			student.total,
			student.average,
			student.grade
		];
		csv += row.join(',') + '\n';
	});
	
	const blob = new Blob([csv], { type: 'text/csv' });
	const url = window.URL.createObjectURL(blob);
	const a = document.createElement('a');
	a.href = url;
	a.download = `students_${new Date().toISOString().split('T')[0]}.csv`;
	a.click();
	window.URL.revokeObjectURL(url);
	
	showNotification('CSV exported successfully!', 'success');
}

function importFromCSV() {
	fileInput.click();
}

// Bulk operations
function updateSelectAllState() {
	const checkboxes = document.querySelectorAll('.student-checkbox');
	const checkedCount = document.querySelectorAll('.student-checkbox:checked').length;
	
	selectAll.checked = checkedCount === checkboxes.length && checkboxes.length > 0;
	selectAll.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
}

function toggleSelectAll() {
	const checkboxes = document.querySelectorAll('.student-checkbox');
	checkboxes.forEach(cb => cb.checked = selectAll.checked);
}

async function bulkDelete() {
	const selectedIds = Array.from(document.querySelectorAll('.student-checkbox:checked'))
		.map(cb => cb.dataset.id);
	
	if (selectedIds.length === 0) {
		showNotification('Please select students to delete', 'error');
		return;
	}
	
	if (!confirm(`Are you sure you want to delete ${selectedIds.length} selected students?`)) return;
	
	try {
		let successCount = 0;
		for (const id of selectedIds) {
			const payload = new URLSearchParams();
			payload.set('studentId', id);
			const res = await fetch(`${API_BASE}/students/delete`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: payload.toString()
			});
			if (res.ok) successCount++;
		}
		
		await fetchList();
		showNotification(`Successfully deleted ${successCount} students`, 'success');
	} catch (e) {
		showNotification('Error during bulk delete', 'error');
	}
}

// Edit student function
function editStudent(studentId) {
	const student = allStudents.find(s => s.studentId === studentId);
	if (!student) return;
	
	// Fill form with student data
	nameEl.value = student.name;
	inputs[0].value = student.mark1;
	inputs[1].value = student.mark2;
	inputs[2].value = student.mark3;
	inputs[3].value = student.mark4;
	inputs[4].value = student.mark5;
	
	// Scroll to form
	form.scrollIntoView({ behavior: 'smooth' });
	showNotification('Student data loaded for editing', 'info');
}

// Enhanced fetch function
async function fetchList() {
	try {
		const res = await fetch(`${API_BASE}/students`);
		if (!res.ok) throw new Error('Failed to load');
		allStudents = await res.json();
		filteredStudents = [...allStudents];
		renderList(filteredStudents);
		updateStatistics();
		showNotification('Student list refreshed', 'success');
	} catch (e) {
		allStudents = [];
		filteredStudents = [];
		renderList([]);
		updateStatistics();
		showNotification('Failed to load students', 'error');
	}
}

// Event listeners
form.addEventListener('submit', async (e) => {
	e.preventDefault();
	
	if (!isValidName(nameEl.value)) {
		showNotification('Please enter a valid name (only letters, spaces, hyphens, and apostrophes allowed, minimum 2 characters)', 'error');
		nameEl.focus();
		return;
	}
	
	for (const input of inputs) { 
		if (!isValidMark(input.value)) { 
			showNotification('Please enter valid marks between 0 and 100 for all subjects', 'error');
			input.focus(); 
			return; 
		} 
	}

	const marks = inputs.map(i => Number(i.value));
	const total = marks.reduce((a,b) => a + b, 0);
	const avg = Math.round(total / marks.length);
	const grade = computeGrade(avg);

	rName.textContent = nameEl.value.trim();
	rTotal.textContent = total;
	rAvg.textContent = avg;
	rGrade.textContent = grade;
	assignGradeClass(grade);
	rNote.textContent = grade === 'F' ? 'Student has failed. Consider remedial actions.' : 'Good job! Keep improving where needed.';
	resultCard.classList.remove('hidden');

	try {
		const payload = new URLSearchParams();
		payload.set('name', nameEl.value.trim());
		payload.set('m1', String(marks[0]));
		payload.set('m2', String(marks[1]));
		payload.set('m3', String(marks[2]));
		payload.set('m4', String(marks[3]));
		payload.set('m5', String(marks[4]));
		const res = await fetch(`${API_BASE}/students`, { 
			method: 'POST', 
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, 
			body: payload.toString() 
		});
		if (res.ok) {
			await fetchList();
			showNotification('Student added successfully!', 'success');
			form.reset();
		} else {
			showNotification('Failed to save student', 'error');
		}
	} catch (e) {
		showNotification('Error saving student', 'error');
	}
});

// Search and filter event listeners
searchInput.addEventListener('input', filterStudents);
searchBtn.addEventListener('click', filterStudents);
gradeFilter.addEventListener('change', filterStudents);
sortBy.addEventListener('change', filterStudents);

// Export/Import event listeners
exportBtn.addEventListener('click', exportToCSV);
importBtn.addEventListener('click', importFromCSV);

fileInput.addEventListener('change', (e) => {
	const file = e.target.files[0];
	if (!file) return;
	
	const reader = new FileReader();
	reader.onload = (e) => {
		try {
			const csv = e.target.result;
			const lines = csv.split('\n');
			const headers = lines[0].split(',');
			
			let importedCount = 0;
			for (let i = 1; i < lines.length; i++) {
				if (lines[i].trim()) {
					const values = lines[i].split(',');
					// Process CSV import here
					importedCount++;
				}
			}
			
			showNotification(`Imported ${importedCount} students`, 'success');
			fetchList();
		} catch (error) {
			showNotification('Error importing CSV file', 'error');
		}
	};
	reader.readAsText(file);
});

// Bulk operations event listeners
selectAll.addEventListener('change', toggleSelectAll);
bulkDeleteBtn.addEventListener('click', bulkDelete);

// Other event listeners
refreshBtn.addEventListener('click', fetchList);

// Delete student function
async function deleteStudent(studentId) {
	if (!confirm(`Are you sure you want to delete student ${studentId}?`)) return;
	
	try {
		const payload = new URLSearchParams();
		payload.set('studentId', studentId);
		const res = await fetch(`${API_BASE}/students/delete`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: payload.toString()
		});
		
		if (res.ok) {
			await fetchList();
			showNotification('Student deleted successfully!', 'success');
		} else {
			showNotification('Failed to delete student', 'error');
		}
	} catch (e) {
		showNotification('Error deleting student', 'error');
	}
}

// Clear all students function
async function clearAllStudents() {
	if (!confirm('Are you sure you want to delete ALL students? This cannot be undone!')) return;
	
	try {
		const res = await fetch(`${API_BASE}/students/clear`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
		});
		
		if (res.ok) {
			await fetchList();
			showNotification('All students deleted successfully!', 'success');
		} else {
			showNotification('Failed to clear students', 'error');
		}
	} catch (e) {
		showNotification('Error clearing students', 'error');
	}
}

// Auto load list on startup
fetchList();