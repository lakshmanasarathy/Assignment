const API_BASE = "";

let currentStaffTicketId = null;
let currentStudentTicketId = null;


// ================================
// COMMON
// ================================

function getToken() {
    return localStorage.getItem("token");
}

function getRole() {
    return localStorage.getItem("role");
}

function getEmail() {
    return localStorage.getItem("email");
}

function logout() {

    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("email");

    window.location.href = "/index.html";
}


async function apiRequest(
    url,
    options = {}
) {

    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (token) {
        headers["Authorization"] =
            "Bearer " + token;
    }

    const response =
        await fetch(API_BASE + url, {
            ...options,
            headers
        });

    if (response.status === 401 ||
        response.status === 403) {

        throw new Error(
            "You are not authorized to perform this action."
        );
    }

    const text = await response.text();

    let data = null;

    if (text) {

        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }

    if (!response.ok) {

        let message =
            data?.message ||
            "Request failed";

        throw new Error(message);
    }

    return data;
}


function showMessage(
    elementId,
    message,
    type = "success"
) {

    const element =
        document.getElementById(elementId);

    if (!element) {
        return;
    }

    element.textContent = message;
    element.className =
        "message " + type;
}


// ================================
// LOGIN
// ================================

const loginForm =
    document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();

            const email =
                document.getElementById(
                    "loginEmail"
                ).value.trim();

            const password =
                document.getElementById(
                    "loginPassword"
                ).value;

            try {

                const result =
                    await apiRequest(
                        "/api/auth/login",
                        {
                            method: "POST",
                            body: JSON.stringify({
                                email: email,
                                password: password
                            })
                        }
                    );

                localStorage.setItem(
                    "token",
                    result.token
                );

                localStorage.setItem(
                    "role",
                    result.role
                );

                localStorage.setItem(
                    "email",
                    email
                );

                if (result.role === "STUDENT") {

                    window.location.href =
                        "/student.html";

                } else if (result.role === "STAFF") {

                    window.location.href =
                        "/staff.html";

                } else {

                    showMessage(
                        "loginMessage",
                        "Unsupported user role.",
                        "error"
                    );
                }

            } catch (error) {

                showMessage(
                    "loginMessage",
                    error.message,
                    "error"
                );
            }
        }
    );
}


// ================================
// STUDENT DASHBOARD
// ================================

if (window.location.pathname.endsWith(
    "/student.html"
)) {

    if (!getToken()) {

        window.location.href =
            "/index.html";

    } else {

        const emailElement =
            document.getElementById(
                "studentEmail"
            );

        if (emailElement) {
            emailElement.textContent =
                getEmail();
        }

        loadStudentTickets();
    }
}


async function loadStudentTickets() {

    try {

        const tickets =
            await apiRequest(
                "/api/tickets/my"
            );

        renderStudentTickets(tickets);
        updateStudentStats(tickets);

    } catch (error) {

        showMessage(
            "studentMessage",
            error.message,
            "error"
        );
    }
}


function updateStudentStats(tickets) {

    const total =
        tickets.length;

    const open =
        tickets.filter(
            ticket =>
                ticket.status === "OPEN"
        ).length;

    const progress =
        tickets.filter(
            ticket =>
                ticket.status === "IN_PROGRESS"
        ).length;

    const resolved =
        tickets.filter(
            ticket =>
                ticket.status === "RESOLVED"
        ).length;

    document.getElementById(
        "totalTickets"
    ).textContent = total;

    document.getElementById(
        "openTickets"
    ).textContent = open;

    document.getElementById(
        "progressTickets"
    ).textContent = progress;

    document.getElementById(
        "resolvedTickets"
    ).textContent = resolved;
}


function renderStudentTickets(
    tickets
) {

    const table =
        document.getElementById(
            "studentTicketTable"
        );

    if (!table) {
        return;
    }

    table.innerHTML = "";

    if (!tickets.length) {

        table.innerHTML = `
            <tr>
                <td colspan="7">
                    No tickets found.
                </td>
            </tr>
        `;

        return;
    }

    tickets.forEach(ticket => {

        const row =
            document.createElement("tr");

        row.innerHTML = `
            <td>${escapeHtml(ticket.ticketNumber)}</td>

            <td>
                ${escapeHtml(ticket.subject)}
            </td>

            <td>
                ${formatCategory(ticket.category)}
            </td>

            <td>
                <span class="${priorityClass(ticket.priority)}">
                    ${formatPriority(ticket.priority)}
                </span>
            </td>

            <td>
                ${statusBadge(ticket.status)}
            </td>

            <td>
                ${formatDate(ticket.createdAt)}
            </td>

            <td>

                <button
                    class="action-btn"
                    type="button"
                    onclick="viewStudentTicket(${ticket.id})">

                    View

                </button>

            </td>
        `;

        table.appendChild(row);
    });
}


// ================================
// CREATE STUDENT TICKET
// ================================

const ticketForm =
    document.getElementById("ticketForm");

if (ticketForm) {

    ticketForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();

            const request = {

                subject:
                    document.getElementById(
                        "ticketSubject"
                    ).value.trim(),

                description:
                    document.getElementById(
                        "ticketDescription"
                    ).value.trim(),

                category:
                    document.getElementById(
                        "ticketCategory"
                    ).value,

                priority:
                    document.getElementById(
                        "ticketPriority"
                    ).value
            };

            try {

                await apiRequest(
                    "/api/tickets",
                    {
                        method: "POST",
                        body: JSON.stringify(request)
                    }
                );

                closeTicketModal();

                ticketForm.reset();

                showMessage(
                    "studentMessage",
                    "Ticket created successfully."
                );

                loadStudentTickets();

            } catch (error) {

                showMessage(
                    "studentMessage",
                    error.message,
                    "error"
                );
            }
        }
    );
}


function openTicketModal() {

    const modal =
        document.getElementById(
            "ticketModal"
        );

    if (modal) {
        modal.classList.add("active");
    }
}


function closeTicketModal() {

    const modal =
        document.getElementById(
            "ticketModal"
        );

    if (modal) {
        modal.classList.remove("active");
    }
}


// ================================
// STUDENT TICKET DETAILS
// ================================

async function viewStudentTicket(
    ticketId
) {

    currentStudentTicketId =
        ticketId;

    try {

        const tickets =
            await apiRequest(
                "/api/tickets/my"
            );

        const ticket =
            tickets.find(
                item =>
                    item.id === ticketId
            );

        if (!ticket) {
            throw new Error(
                "Ticket not found."
            );
        }

        const details =
            document.getElementById(
                "studentTicketDetails"
            );

        details.innerHTML = `

            <div class="detail-grid">

                <div class="detail-item">
                    <span>Ticket Number</span>
                    <strong>
                        ${escapeHtml(ticket.ticketNumber)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Status</span>
                    <strong>
                        ${statusBadge(ticket.status)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Subject</span>
                    <strong>
                        ${escapeHtml(ticket.subject)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Category</span>
                    <strong>
                        ${formatCategory(ticket.category)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Priority</span>
                    <strong>
                        ${formatPriority(ticket.priority)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Created</span>
                    <strong>
                        ${formatDate(ticket.createdAt)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Due At</span>
                    <strong>
                        ${formatDate(ticket.dueAt)}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Assigned To</span>
                    <strong>
                        ${
                            ticket.assignedTo
                                ? escapeHtml(
                                    ticket.assignedTo.email
                                )
                                : "Not assigned"
                        }
                    </strong>
                </div>

            </div>

            <div class="management-section">

                <h3>Description</h3>

                <p>
                    ${escapeHtml(ticket.description)}
                </p>

            </div>

            ${
                ticket.resolution
                    ? `
                        <div class="management-section">

                            <h3>Resolution</h3>

                            <p>
                                ${escapeHtml(
                                    ticket.resolution
                                )}
                            </p>

                        </div>
                    `
                    : ""
            }

            <div class="management-section">

                <h3>Comments</h3>

                <div id="studentComments"
                     class="comments-container">

                    Loading comments...

                </div>

            </div>

            ${
                ticket.status === "RESOLVED"
                    ? `
                        <div class="modal-actions">

                            <button
                                type="button"
                                class="primary-btn"
                                onclick="closeStudentTicket()">

                                Close Ticket

                            </button>

                        </div>
                    `
                    : ""
            }

        `;

        document.getElementById(
            "studentTicketDetailsModal"
        ).classList.add("active");

        await loadStudentComments(ticketId);

    } catch (error) {

        showMessage(
            "studentMessage",
            error.message,
            "error"
        );
    }
}


async function loadStudentComments(
    ticketId
) {

    const container =
        document.getElementById(
            "studentComments"
        );

    if (!container) {
        return;
    }

    try {

        const comments =
            await apiRequest(
                `/api/tickets/${ticketId}/comments`
            );

        if (!comments.length) {

            container.innerHTML =
                "No comments yet.";

            return;
        }

        container.innerHTML =
            comments.map(
                comment => `

                    <div class="comment">

                        <div class="comment-user">
                            ${escapeHtml(
                                comment.user?.name ||
                                comment.user?.email ||
                                "User"
                            )}
                        </div>

                        <div class="comment-date">
                            ${formatDate(
                                comment.createdAt
                            )}
                        </div>

                        <div class="comment-message">
                            ${escapeHtml(
                                comment.message
                            )}
                        </div>

                    </div>

                `
            ).join("");

    } catch (error) {

        container.innerHTML =
            "Unable to load comments.";
    }
}


async function closeStudentTicket() {

    if (!currentStudentTicketId) {
        return;
    }

    if (!confirm(
        "Are you sure you want to close this ticket?"
    )) {
        return;
    }

    try {

        await apiRequest(
            `/api/tickets/${currentStudentTicketId}/close`,
            {
                method: "PUT"
            }
        );

        closeStudentDetails();

        showMessage(
            "studentMessage",
            "Ticket closed successfully."
        );

        loadStudentTickets();

    } catch (error) {

        showMessage(
            "studentMessage",
            error.message,
            "error"
        );
    }
}


function closeStudentDetails() {

    const modal =
        document.getElementById(
            "studentTicketDetailsModal"
        );

    if (modal) {
        modal.classList.remove("active");
    }
}


// ================================
// STAFF DASHBOARD
// ================================

if (window.location.pathname.endsWith(
    "/staff.html"
)) {

    if (!getToken()) {

        window.location.href =
            "/index.html";

    } else {

        const emailElement =
            document.getElementById(
                "staffEmail"
            );

        if (emailElement) {
            emailElement.textContent =
                getEmail();
        }

        loadStaffTickets();
    }
}


async function loadStaffTickets() {

    try {

        const tickets =
            await apiRequest(
                "/api/staff/tickets"
            );

        renderStaffTickets(tickets);
        updateStaffStats(tickets);

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


function updateStaffStats(
    tickets
) {

    document.getElementById(
        "staffTotal"
    ).textContent =
        tickets.length;

    document.getElementById(
        "staffUnassigned"
    ).textContent =
        tickets.filter(
            ticket =>
                !ticket.assignedTo
        ).length;

    document.getElementById(
        "staffProgress"
    ).textContent =
        tickets.filter(
            ticket =>
                ticket.status === "IN_PROGRESS"
        ).length;

    document.getElementById(
        "staffResolved"
    ).textContent =
        tickets.filter(
            ticket =>
                ticket.status === "RESOLVED"
        ).length;
}


function renderStaffTickets(
    tickets
) {

    const table =
        document.getElementById(
            "staffTicketTable"
        );

    if (!table) {
        return;
    }

    table.innerHTML = "";

    if (!tickets.length) {

        table.innerHTML = `
            <tr>
                <td colspan="9">
                    No tickets found.
                </td>
            </tr>
        `;

        return;
    }

    tickets.forEach(ticket => {

        const row =
            document.createElement("tr");

        row.innerHTML = `

            <td>
                ${escapeHtml(
                    ticket.ticketNumber
                )}
            </td>

            <td>
                ${
                    ticket.student
                        ? escapeHtml(
                            ticket.student.name ||
                            ticket.student.email
                        )
                        : "-"
                }
            </td>

            <td>
                ${escapeHtml(
                    ticket.subject
                )}
            </td>

            <td>
                ${formatCategory(
                    ticket.category
                )}
            </td>

            <td>
                <span class="${priorityClass(
                    ticket.priority
                )}">
                    ${formatPriority(
                        ticket.priority
                    )}
                </span>
            </td>

            <td>
                ${statusBadge(
                    ticket.status
                )}
            </td>

            <td>
                ${
                    ticket.assignedTo
                        ? escapeHtml(
                            ticket.assignedTo.email
                        )
                        : "Unassigned"
                }
            </td>

            <td>
                ${slaDisplay(ticket)}
            </td>

            <td>

                <button
                    type="button"
                    class="action-btn"
                    onclick="openStaffTicket(
                        ${ticket.id}
                    )">

                    Manage

                </button>

            </td>
        `;

        table.appendChild(row);
    });
}


// ================================
// STAFF FILTERS
// ================================

async function applyStaffFilters() {

    const status =
        document.getElementById(
            "filterStatus"
        ).value;

    const priority =
        document.getElementById(
            "filterPriority"
        ).value;

    const category =
        document.getElementById(
            "filterCategory"
        ).value;

    const assignment =
        document.getElementById(
            "filterAssignment"
        ).value;

    const params =
        new URLSearchParams();

    if (status) {
        params.set(
            "status",
            status
        );
    }

    if (priority) {
        params.set(
            "priority",
            priority
        );
    }

    if (category) {
        params.set(
            "category",
            category
        );
    }

    if (assignment) {
        params.set(
            "assignment",
            assignment
        );
    }

    try {

        const tickets =
            await apiRequest(
                "/api/staff/tickets/filter?"
                + params.toString()
            );

        renderStaffTickets(tickets);

        updateStaffStats(tickets);

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


function clearStaffFilters() {

    document.getElementById(
        "filterStatus"
    ).value = "";

    document.getElementById(
        "filterPriority"
    ).value = "";

    document.getElementById(
        "filterCategory"
    ).value = "";

    document.getElementById(
        "filterAssignment"
    ).value = "ALL";

    loadStaffTickets();
}


// ================================
// STAFF TICKET MANAGEMENT
// ================================

async function openStaffTicket(
    ticketId
) {

    currentStaffTicketId =
        ticketId;

    try {

        const ticket =
            await apiRequest(
                `/api/staff/tickets/${ticketId}`
            );

        document.getElementById(
            "staffTicketDetails"
        ).innerHTML = `

            <div class="detail-grid">

                <div class="detail-item">
                    <span>Ticket Number</span>
                    <strong>
                        ${escapeHtml(
                            ticket.ticketNumber
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Student</span>
                    <strong>
                        ${
                            ticket.student
                                ? escapeHtml(
                                    ticket.student.email
                                )
                                : "-"
                        }
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Subject</span>
                    <strong>
                        ${escapeHtml(
                            ticket.subject
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Category</span>
                    <strong>
                        ${formatCategory(
                            ticket.category
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Status</span>
                    <strong>
                        ${statusBadge(
                            ticket.status
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Priority</span>
                    <strong>
                        ${formatPriority(
                            ticket.priority
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Created</span>
                    <strong>
                        ${formatDate(
                            ticket.createdAt
                        )}
                    </strong>
                </div>

                <div class="detail-item">
                    <span>Due</span>
                    <strong>
                        ${formatDate(
                            ticket.dueAt
                        )}
                    </strong>
                </div>

            </div>

            <div class="management-section">

                <h3>Description</h3>

                <p>
                    ${escapeHtml(
                        ticket.description
                    )}
                </p>

            </div>

        `;

        document.getElementById(
            "staffStatus"
        ).value = ticket.status;

        document.getElementById(
            "staffPriority"
        ).value = ticket.priority;

        document.getElementById(
            "assignedStaffEmail"
        ).value =
            ticket.assignedTo
                ? ticket.assignedTo.email
                : "";

        document.getElementById(
            "staffResolution"
        ).value =
            ticket.resolution || "";

        document.getElementById(
            "staffTicketModal"
        ).classList.add("active");

        await loadStaffComments(ticketId);

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


async function updateTicketStatus() {

    if (!currentStaffTicketId) {
        return;
    }

    const status =
        document.getElementById(
            "staffStatus"
        ).value;

    try {

        await apiRequest(
            `/api/staff/tickets/${currentStaffTicketId}/status`,
            {
                method: "PUT",
                body: JSON.stringify({
                    status: status
                })
            }
        );

        showMessage(
            "staffMessage",
            "Status updated successfully."
        );

        await openStaffTicket(
            currentStaffTicketId
        );

        await loadStaffTickets();

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


async function updateTicketPriority() {

    if (!currentStaffTicketId) {
        return;
    }

    const priority =
        document.getElementById(
            "staffPriority"
        ).value;

    try {

        await apiRequest(
            `/api/staff/tickets/${currentStaffTicketId}/priority`,
            {
                method: "PUT",
                body: JSON.stringify({
                    priority: priority
                })
            }
        );

        showMessage(
            "staffMessage",
            "Priority updated successfully."
        );

        await openStaffTicket(
            currentStaffTicketId
        );

        await loadStaffTickets();

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


async function assignTicket() {

    if (!currentStaffTicketId) {
        return;
    }

    const staffEmail =
        document.getElementById(
            "assignedStaffEmail"
        ).value.trim();

    if (!staffEmail) {

        showMessage(
            "staffMessage",
            "Staff email is required.",
            "error"
        );

        return;
    }

    try {

        await apiRequest(
            `/api/staff/tickets/${currentStaffTicketId}/assign`,
            {
                method: "PUT",
                body: JSON.stringify({
                    staffEmail: staffEmail
                })
            }
        );

        showMessage(
            "staffMessage",
            "Ticket assigned successfully."
        );

        await openStaffTicket(
            currentStaffTicketId
        );

        await loadStaffTickets();

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


async function updateResolution() {

    if (!currentStaffTicketId) {
        return;
    }

    const resolution =
        document.getElementById(
            "staffResolution"
        ).value.trim();

    if (!resolution) {

        showMessage(
            "staffMessage",
            "Resolution is required.",
            "error"
        );

        return;
    }

    try {

        await apiRequest(
            `/api/staff/tickets/${currentStaffTicketId}/resolution`,
            {
                method: "PUT",
                body: JSON.stringify({
                    resolution: resolution
                })
            }
        );

        showMessage(
            "staffMessage",
            "Resolution saved successfully."
        );

        await openStaffTicket(
            currentStaffTicketId
        );

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


// ================================
// STAFF COMMENTS
// ================================

async function loadStaffComments(
    ticketId
) {

    const container =
        document.getElementById(
            "staffComments"
        );

    if (!container) {
        return;
    }

    try {

        const comments =
            await apiRequest(
                `/api/tickets/${ticketId}/comments`
            );

        if (!comments.length) {

            container.innerHTML =
                "No comments yet.";

            return;
        }

        container.innerHTML =
            comments.map(
                comment => `

                    <div class="comment">

                        <div class="comment-user">
                            ${escapeHtml(
                                comment.user?.name ||
                                comment.user?.email ||
                                "User"
                            )}
                        </div>

                        <div class="comment-date">
                            ${formatDate(
                                comment.createdAt
                            )}
                        </div>

                        <div class="comment-message">
                            ${escapeHtml(
                                comment.message
                            )}
                        </div>

                    </div>

                `
            ).join("");

    } catch (error) {

        container.innerHTML =
            "Unable to load comments.";
    }
}


async function addStaffComment() {

    if (!currentStaffTicketId) {
        return;
    }

    const input =
        document.getElementById(
            "staffComment"
        );

    const message =
        input.value.trim();

    if (!message) {

        showMessage(
            "staffMessage",
            "Comment message is required.",
            "error"
        );

        return;
    }

    try {

        await apiRequest(
            `/api/tickets/${currentStaffTicketId}/comments`,
            {
                method: "POST",
                body: JSON.stringify({
                    message: message
                })
            }
        );

        input.value = "";

        await loadStaffComments(
            currentStaffTicketId
        );

        showMessage(
            "staffMessage",
            "Comment added successfully."
        );

    } catch (error) {

        showMessage(
            "staffMessage",
            error.message,
            "error"
        );
    }
}


function closeStaffModal() {

    const modal =
        document.getElementById(
            "staffTicketModal"
        );

    if (modal) {
        modal.classList.remove("active");
    }

    currentStaffTicketId = null;
}


// ================================
// FORMATTERS
// ================================

function formatCategory(
    category
) {

    if (!category) {
        return "-";
    }

    return category
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g,
            character =>
                character.toUpperCase()
        );
}


function formatPriority(
    priority
) {

    if (!priority) {
        return "-";
    }

    return priority
        .toLowerCase()
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}


function priorityClass(
    priority
) {

    switch (priority) {

        case "LOW":
            return "priority-low";

        case "MEDIUM":
            return "priority-medium";

        case "HIGH":
            return "priority-high";

        case "CRITICAL":
            return "priority-critical";

        default:
            return "";
    }
}


function statusBadge(
    status
) {

    let className =
        "badge-open";

    switch (status) {

        case "IN_PROGRESS":
            className =
                "badge-progress";
            break;

        case "WAITING_FOR_STUDENT":
            className =
                "badge-waiting";
            break;

        case "RESOLVED":
            className =
                "badge-resolved";
            break;

        case "CLOSED":
            className =
                "badge-closed";
            break;
    }

    return `
        <span class="badge ${className}">
            ${formatStatus(status)}
        </span>
    `;
}


function formatStatus(
    status
) {

    if (!status) {
        return "-";
    }

    return status
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(
            /\b\w/g,
            character =>
                character.toUpperCase()
        );
}


function formatDate(
    date
) {

    if (!date) {
        return "-";
    }

    const parsed =
        new Date(date);

    if (Number.isNaN(
        parsed.getTime()
    )) {
        return date;
    }

    return parsed.toLocaleString();
}


function slaDisplay(
    ticket
) {

    if (!ticket.dueAt) {
        return "-";
    }

    if (
        ticket.status === "RESOLVED" ||
        ticket.status === "CLOSED"
    ) {

        return `
            <span class="badge badge-resolved">
                Completed
            </span>
        `;
    }

    const due =
        new Date(ticket.dueAt);

    const now =
        new Date();

    if (now > due) {

        return `
            <span class="badge"
                  style="background:#fee2e2;color:#991b1b">
                Overdue
            </span>
        `;
    }

    const hours =
        Math.floor(
            (due - now) /
            (1000 * 60 * 60)
        );

    if (hours <= 8) {

        return `
            <span class="badge"
                  style="background:#fef3c7;color:#92400e">
                Due Soon
            </span>
        `;
    }

    return `
        <span class="badge badge-resolved">
            On Time
        </span>
    `;
}


function escapeHtml(
    value
) {

    if (value === null ||
        value === undefined) {

        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}