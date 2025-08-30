const API_BASE = 'http://localhost:8081/api';

const form = document.getElementById('gradeForm');
const resultCard = document.getElementById('resultCard');

const nameEl = document.getElementById('name');
const idEl = document.getElementById('studentId');
const inputs = ['m1','m2','m3','m4','m5'].map(id => document.getElementById(id));

const rName = document.getElementById('rName');
const rId = document.getElementById('rId');
const rTotal = document.getElementById('rTotal');
const rAvg = document.getElementById('rAvg');
const rGrade = document.getElementById('rGrade');
const rNote = document.getElementById('rNote');

const refreshBtn = document.getElementById('refreshBtn');
const tableBody = document.querySelector('#studentsTable tbody');

function isValidName(name) {
	// Only allow letters, spaces, and common name characters (hyphens, apostrophes)
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

function renderList(rows) {
	tableBody.innerHTML = '';
	for (const row of rows) {
		const tr = document.createElement('tr');
		tr.innerHTML = `
			<td>${row.studentId || ''}</td>
			<td>${row.name || ''}</td>
			<td>${row.total ?? ''}</td>
			<td>${row.average ?? ''}</td>
			<td>${row.grade || ''}</td>
		`;
		tableBody.appendChild(tr);
	}
}

async function fetchList() {
	try {
		const res = await fetch(`${API_BASE}/students`);
		if (!res.ok) throw new Error('Failed to load');
		const data = await res.json();
		renderList(data);
	} catch (e) {
		renderList([]);
	}
}

form.addEventListener('submit', async (e) => {
	e.preventDefault();
	
	// Validate name (only letters and spaces)
	if (!isValidName(nameEl.value)) {
		alert('Please enter a valid name (only letters, spaces, hyphens, and apostrophes allowed, minimum 2 characters)');
		nameEl.focus();
		return;
	}
	
	// Validate marks
	for (const input of inputs) { 
		if (!isValidMark(input.value)) { 
			alert('Please enter valid marks between 0 and 100 for all subjects');
			input.focus(); 
			return; 
		} 
	}

	const marks = inputs.map(i => Number(i.value));
	const total = marks.reduce((a,b) => a + b, 0);
	const avg = Math.round(total / marks.length);
	const grade = computeGrade(avg);

	rName.textContent = nameEl.value.trim();
	rId.textContent = idEl.value.trim() || '(auto-generated)';
	rTotal.textContent = total;
	rAvg.textContent = avg;
	rGrade.textContent = grade;
	assignGradeClass(grade);
	rNote.textContent = grade === 'F' ? 'Student has failed. Consider remedial actions.' : 'Good job! Keep improving where needed.';
	resultCard.classList.remove('hidden');

	// Try to store in backend
	try {
		const payload = new URLSearchParams();
		payload.set('name', nameEl.value.trim());
		payload.set('m1', String(marks[0]));
		payload.set('m2', String(marks[1]));
		payload.set('m3', String(marks[2]));
		payload.set('m4', String(marks[3]));
		payload.set('m5', String(marks[4]));
		const res = await fetch(`${API_BASE}/students`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: payload.toString() });
		if (res.ok) {
			const saved = await res.json();
			rId.textContent = saved.studentId || rId.textContent;
			await fetchList();
		}
	} catch {}
});

refreshBtn.addEventListener('click', fetchList);

// Auto load list on startup
fetchList();
