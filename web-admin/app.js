// Mock Databases initialized in LocalStorage for persistence during testing
const initialPickups = [
    { id: "req_101", donorName: "Austin Paul", phone: "+919876543210", email: "austin@janmanas.org", address: "12, Kasturba Gandhi Marg, Connaught Place, New Delhi", date: "2026-07-18", time: "10:00 AM - 01:00 PM", items: ["Clothes", "Books", "Toys"], quantity: "Medium Box (~5kg)", instructions: "Call guard on arrival.", status: "PENDING" },
    { id: "req_102", donorName: "Simran Kaur", phone: "+918877665544", email: "simran.k@example.com", address: "Flat 405, Block B, Green Heights, Gurugram", date: "2026-07-17", time: "02:00 PM - 05:00 PM", items: ["Blankets", "Bicycles", "Electronics"], quantity: "Large bag + Bicycle", instructions: "Bicycle has flat tires.", status: "ASSIGNED" },
    { id: "req_103", donorName: "Rohan Sharma", phone: "+919988776655", email: "rohan.s@example.com", address: "Pocket C-9, Sector 8, Rohini, New Delhi", date: "2026-07-15", time: "09:00 AM - 12:00 PM", items: ["Medicines", "Groceries"], quantity: "Small packet", instructions: "Check medicine expiry.", status: "COMPLETED" }
];

const initialVolunteers = [
    { id: "vol_01", name: "Aman Verma", phone: "+919876543211", email: "aman.verma@example.com", age: 22, gender: "Male", city: "New Delhi", state: "Delhi", skills: ["Teaching", "Social Media", "First Aid"], availability: "Weekends", hoursTracked: 24, status: "Active", joinedDate: "2026-06-12" },
    { id: "vol_02", name: "Meera Nair", phone: "+919988776611", email: "meera.n@example.com", age: 29, gender: "Female", city: "Bengaluru", state: "Karnataka", skills: ["Web Development", "Event Management"], availability: "Flexible", hoursTracked: 10, status: "Active", joinedDate: "2026-07-01" },
    { id: "vol_03", name: "Rahul Das", phone: "+918877665511", email: "rahul.das@example.com", age: 35, gender: "Male", city: "Kolkata", state: "West Bengal", skills: ["First Aid", "Disaster Relief"], availability: "On Call", hoursTracked: 0, status: "Pending", joinedDate: "2026-07-10" }
];

const initialFundraisers = [
    { id: "fund_101", title: "Emergency Heart Surgery for 5-Year-Old Arav", story: "Congenital heart defect requiring urgent open heart surgery at AIIMS, New Delhi.", targetAmount: 250000, raisedAmount: 180000, category: "Medical Emergency", creatorName: "Jan Manas Foundation", isVerified: true, location: "AIIMS, New Delhi" },
    { id: "fund_102", title: "School Fees and Supplies for Slum Children", story: "Funding books, uniforms, and tuition for 30 slum children in Gurgaon.", targetAmount: 50000, raisedAmount: 12000, category: "Education", creatorName: "Shiksha NGO", isVerified: true, location: "Gurugram, Haryana" },
    { id: "fund_103", title: "Assam Flood Relief Food Distribution", story: "Distributing clean water, ration kits, and blankets to flood victims.", targetAmount: 150000, raisedAmount: 145000, category: "Flood Relief", creatorName: "Jan Manas Disaster Response", isVerified: true, location: "Guwahati, Assam" },
    { id: "fund_104", title: " storm shelter rebuild for stray dogs", story: "Tin shed rebuilding and dog food purchase for Noida stray animal center.", targetAmount: 75000, raisedAmount: 5000, category: "Animal Rescue", creatorName: "Paws & Claws Rescue", isVerified: false, location: "Sector 62, Noida" }
];

// LocalStorage helpers
function getDB(key, defaultData) {
    const data = localStorage.getItem(key);
    if (!data) {
        localStorage.setItem(key, JSON.stringify(defaultData));
        return defaultData;
    }
    return JSON.parse(data);
}

function saveDB(key, data) {
    localStorage.setItem(key, JSON.stringify(data));
}

let pickups = getDB("share_pickups", initialPickups);
let volunteers = getDB("share_volunteers", initialVolunteers);
let fundraisers = getDB("share_fundraisers", initialFundraisers);

// Tab switching logic
const navItems = document.querySelectorAll('.nav-item');
const tabPanels = document.querySelectorAll('.tab-panel');

navItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const tabId = item.getAttribute('data-tab');
        switchTab(tabId);
    });
});

function switchTab(tabId) {
    navItems.forEach(nav => nav.classList.remove('active'));
    tabPanels.forEach(panel => panel.classList.remove('active'));

    const activeNav = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    const activePanel = document.getElementById(`tab-${tabId}`);
    
    if (activeNav && activePanel) {
        activeNav.classList.add('active');
        activePanel.classList.add('active');
        
        // Update header texts
        const titleEl = document.getElementById('tab-title');
        const subtitleEl = document.getElementById('tab-subtitle');
        
        if (tabId === 'dashboard') {
            titleEl.textContent = "Dashboard Overview";
            subtitleEl.textContent = "NGO management console and metrics";
        } else if (tabId === 'pickups') {
            titleEl.textContent = "Doorstep Pickups Management";
            subtitleEl.textContent = "Assign drivers and track status of donations";
            renderPickupsTable();
        } else if (tabId === 'fundraisers') {
            titleEl.textContent = "Fundraisers Platform Manager";
            subtitleEl.textContent = "Verify campaign documents and launch new fundraisers";
            renderFundraisersTable();
        } else if (tabId === 'volunteers') {
            titleEl.textContent = "Volunteers Database";
            subtitleEl.textContent = "Verify registrations, log service hours, and issue certificates";
            renderVolunteersTable();
        }
    }
}

// Initial counts update
function updateDashboardCounts() {
    document.getElementById('count-pickups').textContent = pickups.filter(p => p.status !== 'COMPLETED').length;
    
    const totalRaised = fundraisers.reduce((sum, f) => sum + f.raisedAmount, 0);
    document.getElementById('count-funds').textContent = "₹" + totalRaised.toLocaleString('en-IN');
    
    document.getElementById('count-vols').textContent = volunteers.filter(v => v.status === 'Active').length;
    document.getElementById('count-pending-campaigns').textContent = fundraisers.filter(f => !f.isVerified).length;
}

// Render Pickups Table
function renderPickupsTable() {
    const tbody = document.getElementById('pickups-table-body');
    tbody.innerHTML = '';
    
    pickups.forEach(pickup => {
        const tr = document.createElement('tr');
        tr.style.cursor = 'pointer';
        tr.addEventListener('click', () => showPickupDetail(pickup));
        
        const statusClass = pickup.status.toLowerCase();
        
        tr.innerHTML = `
            <td><strong>${pickup.donorName}</strong><br><span style="font-size:11px;color:gray">${pickup.phone}</span></td>
            <td><span style="display:inline-block;max-width:180px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" title="${pickup.address}">${pickup.address}</span></td>
            <td>${pickup.items.join(', ')}</td>
            <td>${pickup.date}<br><span style="font-size:11px;color:gray">${pickup.time}</span></td>
            <td><span class="badge ${statusClass}">${pickup.status}</span></td>
            <td>
                ${pickup.status === 'PENDING' ? `<button class="table-btn primary" onclick="event.stopPropagation(); updatePickupStatus('${pickup.id}', 'ASSIGNED')">Assign</button>` : ''}
                ${pickup.status === 'ASSIGNED' ? `<button class="table-btn primary" onclick="event.stopPropagation(); updatePickupStatus('${pickup.id}', 'PICKED_UP')">Pick Up</button>` : ''}
                ${pickup.status === 'PICKED_UP' ? `<button class="table-btn primary" onclick="event.stopPropagation(); updatePickupStatus('${pickup.id}', 'DELIVERED')">Deliver</button>` : ''}
                ${pickup.status === 'DELIVERED' ? `<button class="table-btn primary" onclick="event.stopPropagation(); updatePickupStatus('${pickup.id}', 'COMPLETED')">Complete</button>` : ''}
                ${pickup.status === 'COMPLETED' ? `<span style="color:var(--primary);font-weight:bold">Done</span>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function showPickupDetail(pickup) {
    const detailsBox = document.getElementById('pickup-details-box');
    detailsBox.innerHTML = `
        <div class="detail-info-row">
            <span>Donor Name</span>
            <p>${pickup.donorName}</p>
        </div>
        <div class="detail-info-row">
            <span>Contact Details</span>
            <p>${pickup.phone} / ${pickup.email || 'N/A'}</p>
        </div>
        <div class="detail-info-row">
            <span>Pickup Address</span>
            <p>${pickup.address}</p>
        </div>
        <div class="detail-info-row">
            <span>Items List</span>
            <p>${pickup.items.join(', ')} (${pickup.quantity})</p>
        </div>
        <div class="detail-info-row">
            <span>Instructions</span>
            <p>${pickup.instructions || 'None'}</p>
        </div>
        <div class="detail-info-row">
            <span>Status</span>
            <p><span class="badge ${pickup.status.toLowerCase()}">${pickup.status}</span></p>
        </div>
    `;
}

function updatePickupStatus(id, newStatus) {
    pickups = pickups.map(p => p.id === id ? { ...p, status: newStatus } : p);
    saveDB("share_pickups", pickups);
    renderPickupsTable();
    updateDashboardCounts();
    
    // Refresh details panel if currently shown
    const activeDetail = pickups.find(p => p.id === id);
    if (activeDetail) showPickupDetail(activeDetail);
}

// Render Fundraisers Table
function renderFundraisersTable() {
    const tbody = document.getElementById('fundraisers-table-body');
    tbody.innerHTML = '';
    
    fundraisers.forEach(fund => {
        const tr = document.createElement('tr');
        
        tr.innerHTML = `
            <td><strong>${fund.title}</strong><br><span style="font-size:11px;color:gray">${fund.location}</span></td>
            <td>${fund.category}</td>
            <td>${fund.creatorName}</td>
            <td>₹${fund.targetAmount.toLocaleString('en-IN')}</td>
            <td>₹${fund.raisedAmount.toLocaleString('en-IN')}</td>
            <td><span class="badge ${fund.isVerified ? 'verified' : 'unverified'}">${fund.isVerified ? 'Verified' : 'Pending'}</span></td>
            <td>
                ${!fund.isVerified ? `<button class="table-btn primary" onclick="approveFundraiser('${fund.id}')">Verify & Publish</button>` : `<span style="color:var(--primary);font-weight:bold">Active</span>`}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function approveFundraiser(id) {
    fundraisers = fundraisers.map(f => f.id === id ? { ...f, isVerified: true } : f);
    saveDB("share_fundraisers", fundraisers);
    renderFundraisersTable();
    updateDashboardCounts();
}

// Add fundraiser submit form
const newFundraiserForm = document.getElementById('new-fundraiser-form');
newFundraiserForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const title = document.getElementById('fund-title').value;
    const category = document.getElementById('fund-category').value;
    const type = document.getElementById('fund-type').value;
    const targetAmount = parseFloat(document.getElementById('fund-target').value);
    const locationInput = document.getElementById('fund-location').value;
    const story = document.getElementById('fund-story').value;
    
    const newFund = {
        id: "fund_" + Date.now(),
        title: title,
        category: category,
        creatorName: type === 'NGO' ? "Partner NGO" : "Verified Individual",
        targetAmount: targetAmount,
        raisedAmount: 0,
        isVerified: true, // admin created is pre-verified!
        location: locationInput,
        story: story
    };
    
    fundraisers.unshift(newFund);
    saveDB("share_fundraisers", fundraisers);
    renderFundraisersTable();
    updateDashboardCounts();
    
    newFundraiserForm.reset();
    alert("Campaign launched directly to consumer app successfully!");
});

// Render Volunteers Table
function renderVolunteersTable() {
    const tbody = document.getElementById('volunteers-table-body');
    tbody.innerHTML = '';
    
    const searchQuery = document.getElementById('vol-search').value.toLowerCase();
    const filterSkill = document.getElementById('vol-filter-skill').value;
    
    const filteredVolunteers = volunteers.filter(vol => {
        const matchesSearch = vol.name.toLowerCase().includes(searchQuery) || vol.skills.join(', ').toLowerCase().includes(searchQuery) || vol.city.toLowerCase().includes(searchQuery);
        const matchesSkill = filterSkill === 'All' || vol.skills.includes(filterSkill);
        return matchesSearch && matchesSkill;
    });
    
    filteredVolunteers.forEach(vol => {
        const tr = document.createElement('tr');
        const statusClass = vol.status === 'Active' ? 'active' : 'pending';
        
        tr.innerHTML = `
            <td><strong>${vol.name}</strong><br><span style="font-size:11px;color:gray">Joined: ${vol.joinedDate}</span></td>
            <td>${vol.email}</td>
            <td>${vol.phone}</td>
            <td>${vol.city}, ${vol.state}</td>
            <td><span style="font-size:12px">${vol.skills.join(', ')}</span></td>
            <td>${vol.availability}</td>
            <td><strong>${vol.hoursTracked} hours</strong></td>
            <td><span class="badge ${statusClass}">${vol.status}</span></td>
            <td>
                ${vol.status === 'Pending' ? `<button class="table-btn primary" onclick="verifyVolunteer('${vol.id}')">Approve</button>` : ''}
                ${vol.status === 'Active' ? `<button class="table-btn primary" onclick="logVolunteerHours('${vol.id}')">+2 Hrs</button>` : ''}
                ${vol.hoursTracked >= 10 ? `<button class="table-btn secondary" onclick="openCertModal('${vol.name}', ${vol.hoursTracked})">Cert</button>` : ''}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// Search & Filter event listeners for Volunteers
document.getElementById('vol-search').addEventListener('input', renderVolunteersTable);
document.getElementById('vol-filter-skill').addEventListener('change', renderVolunteersTable);

function verifyVolunteer(id) {
    volunteers = volunteers.map(v => v.id === id ? { ...v, status: 'Active' } : v);
    saveDB("share_volunteers", volunteers);
    renderVolunteersTable();
    updateDashboardCounts();
}

function logVolunteerHours(id) {
    volunteers = volunteers.map(v => v.id === id ? { ...v, hoursTracked: v.hoursTracked + 2 } : v);
    saveDB("share_volunteers", volunteers);
    renderVolunteersTable();
}

// Modal logic for certificate
const modal = document.getElementById('certificate-modal');
const closeBtn = document.querySelector('.close-btn');

closeBtn.addEventListener('click', () => {
    modal.classList.remove('active');
});

window.addEventListener('click', (e) => {
    if (e.target === modal) {
        modal.classList.remove('active');
    }
});

function openCertModal(name, hours) {
    document.getElementById('cert-vol-name').textContent = name;
    document.getElementById('cert-vol-hours').textContent = hours + " Hours";
    document.getElementById('cert-date').textContent = new Date().toISOString().split('T')[0];
    modal.classList.add('active');
}

// Initial Run
updateDashboardCounts();
