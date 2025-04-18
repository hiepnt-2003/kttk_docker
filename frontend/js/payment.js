// js/payment.js
document.addEventListener('DOMContentLoaded', () => {
    // ===== FUNCTIONS =====
    
    // Format tiền tệ
    const formatCurrency = (amount) => {
        return parseInt(amount).toLocaleString('vi-VN') + ' đ';
    };
    
    // Format ngày tháng
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    };
    
    // Tìm kiếm khách hàng theo CMND/CCCD hoặc SĐT
    const searchCustomer = async (searchTerm, nameElement, customerIdElement) => {
        try {
            const customers = await API.searchCustomers(searchTerm);
            
            if (customers && customers.length > 0) {
                const customer = customers[0]; // Lấy kết quả đầu tiên
                
                nameElement.value = customer.fullName;
                customerIdElement.value = customer.id;
                
                return customer.id;
            } else {
                alert('Không tìm thấy khách hàng với thông tin này.');
                return null;
            }
        } catch (error) {
            console.error('Error searching customer:', error);
            alert('Lỗi khi tìm kiếm khách hàng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Load danh sách phòng của khách hàng
    const loadCustomerRooms = async (customerId, selectElement) => {
        try {
            // Lấy danh sách check-in hoạt động của khách hàng
            const checkIns = await API.getActiveCheckInsByCustomer(customerId);
            
            selectElement.innerHTML = '<option value="" selected disabled>-- Chọn phòng --</option>';
            
            if (checkIns && checkIns.length > 0) {
                for (const checkIn of checkIns) {
                    // Lấy thông tin phòng
                    const room = await API.getRoomById(checkIn.roomId);
                    
                    const option = document.createElement('option');
                    option.value = room.id;
                    option.textContent = `${room.roomNumber} - ${room.roomTypeName}`;
                    option.dataset.roomNumber = room.roomNumber;
                    option.dataset.price = room.monthlyPrice;
                    selectElement.appendChild(option);
                }
                return true;
            } else {
                alert('Khách hàng này không có phòng đang thuê.');
                return false;
            }
        } catch (error) {
            console.error('Error loading customer rooms:', error);
            alert('Lỗi khi tải danh sách phòng. Vui lòng thử lại sau.');
            return false;
        }
    };
    
    // Lấy danh sách hóa đơn chưa thanh toán của khách hàng
    const loadPendingInvoices = async (customerId, roomId, selectElement) => {
        try {
            const invoices = await API.getInvoicesByCustomerAndRoom(customerId, roomId, 'PENDING');
            
            selectElement.innerHTML = '<option value="" selected>-- Chọn hóa đơn (nếu có) --</option>';
            
            if (invoices && invoices.length > 0) {
                invoices.forEach(invoice => {
                    const option = document.createElement('option');
                    option.value = invoice.id;
                    option.textContent = `${invoice.invoiceNumber} - ${invoice.monthYear} - ${formatCurrency(invoice.amount)}`;
                    option.dataset.amount = invoice.amount;
                    selectElement.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Error loading pending invoices:', error);
            alert('Lỗi khi tải danh sách hóa đơn. Vui lòng thử lại sau.');
        }
    };
    
    // Tạo hóa đơn mới
    const createInvoice = async (invoiceData) => {
        try {
            const newInvoice = await API.createInvoice(invoiceData);
            return newInvoice;
        } catch (error) {
            console.error('Error creating invoice:', error);
            alert('Lỗi khi tạo hóa đơn. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Thanh toán hóa đơn
    const payInvoice = async (invoiceId, paymentMethod, transactionId, notes) => {
        try {
            const payment = await API.payInvoice(invoiceId, paymentMethod, transactionId, notes);
            return payment;
        } catch (error) {
            console.error('Error paying invoice:', error);
            alert('Lỗi khi thanh toán hóa đơn. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tạo thanh toán mới (không qua hóa đơn)
    const createPayment = async (paymentData) => {
        try {
            const newPayment = await API.createPayment(paymentData);
            return newPayment;
        } catch (error) {
            console.error('Error creating payment:', error);
            alert('Lỗi khi tạo thanh toán. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Load danh sách hóa đơn
    const loadInvoices = async (status = null) => {
        try {
            const invoices = await API.getInvoices(status);
            return invoices;
        } catch (error) {
            console.error('Error loading invoices:', error);
            alert('Lỗi khi tải danh sách hóa đơn. Vui lòng thử lại sau.');
            return [];
        }
    };
    
    // Load danh sách thanh toán
    const loadPayments = async () => {
        try {
            const payments = await API.getAllPayments();
            return payments;
        } catch (error) {
            console.error('Error loading payments:', error);
            alert('Lỗi khi tải danh sách thanh toán. Vui lòng thử lại sau.');
            return [];
        }
    };
    
    // Hiển thị danh sách hóa đơn trong bảng
    const displayInvoices = (invoices, tableBodyElement) => {
        tableBodyElement.innerHTML = '';
        
        if (invoices.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="9" class="text-center">Không có dữ liệu</td>';
            tableBodyElement.appendChild(row);
            return;
        }
        
        invoices.forEach(invoice => {
            const row = document.createElement('tr');
            
            const statusClass = 
                invoice.status === 'PENDING' ? 'bg-warning text-dark' : 
                invoice.status === 'PAID' ? 'bg-success text-white' : 
                invoice.status === 'OVERDUE' ? 'bg-danger text-white' : 
                'bg-secondary text-white';
            
            const statusText = 
                invoice.status === 'PENDING' ? 'Chờ thanh toán' : 
                invoice.status === 'PAID' ? 'Đã thanh toán' : 
                invoice.status === 'OVERDUE' ? 'Quá hạn' : 
                'Đã hủy';
            
            const issuedDate = formatDate(invoice.issueDate);
            const dueDate = formatDate(invoice.dueDate);
            
            row.innerHTML = `
                <td>${invoice.invoiceNumber}</td>
                <td>${invoice.customerName}</td>
                <td>${invoice.roomNumber}</td>
                <td>${invoice.monthYear}</td>
                <td>${formatCurrency(invoice.amount)}</td>
                <td>${issuedDate}</td>
                <td>${dueDate}</td>
                <td><span class="badge ${statusClass}">${statusText}</span></td>
                <td>
                    ${invoice.status === 'PENDING' || invoice.status === 'OVERDUE' ? 
                        `<button class="btn btn-sm btn-success pay-invoice" data-id="${invoice.id}" data-invoice-number="${invoice.invoiceNumber}" data-customer-name="${invoice.customerName}" data-room-number="${invoice.roomNumber}" data-amount="${invoice.amount}">
                            <i class="bi bi-cash-coin me-1"></i> Thanh toán
                        </button>` : 
                        ''
                    }
                    <button class="btn btn-sm btn-info view-invoice" data-id="${invoice.id}">
                        <i class="bi bi-eye me-1"></i> Xem
                    </button>
                    ${(invoice.status === 'PENDING' || invoice.status === 'OVERDUE') ? 
                        `<button class="btn btn-sm btn-danger cancel-invoice" data-id="${invoice.id}">
                            <i class="bi bi-x-circle me-1"></i> Hủy
                        </button>` : 
                        ''
                    }
                </td>
                `;
            
            tableBodyElement.appendChild(row);
        });
        
        // Thêm sự kiện cho các nút trong bảng
        tableBodyElement.querySelectorAll('.pay-invoice').forEach(button => {
            button.addEventListener('click', (e) => {
                const target = e.target.tagName === 'BUTTON' ? e.target : e.target.closest('button');
                const invoiceId = target.dataset.id;
                const invoiceNumber = target.dataset.invoiceNumber;
                const customerName = target.dataset.customerName;
                const roomNumber = target.dataset.roomNumber;
                const amount = target.dataset.amount;
                
                // Điền thông tin vào modal thanh toán
                document.getElementById('payInvoiceId').value = invoiceId;
                document.getElementById('payInvoiceNumber').value = invoiceNumber;
                document.getElementById('payCustomerName').value = customerName;
                document.getElementById('payRoomNumber').value = roomNumber;
                document.getElementById('payAmount').value = formatCurrency(amount);
                
                // Hiển thị modal
                const payInvoiceModal = new bootstrap.Modal(document.getElementById('payInvoiceModal'));
                payInvoiceModal.show();
            });
        });
        
        tableBodyElement.querySelectorAll('.view-invoice').forEach(button => {
            button.addEventListener('click', async (e) => {
                const invoiceId = e.target.dataset.id || e.target.closest('button').dataset.id;
                alert(`Xem chi tiết hóa đơn #${invoiceId}`);
                // Implement chi tiết hóa đơn ở đây
            });
        });
        
        tableBodyElement.querySelectorAll('.cancel-invoice').forEach(button => {
            button.addEventListener('click', async (e) => {
                const invoiceId = e.target.dataset.id || e.target.closest('button').dataset.id;
                
                if (confirm(`Bạn có chắc chắn muốn hủy hóa đơn #${invoiceId}?`)) {
                    try {
                        await API.updateInvoiceStatus(invoiceId, 'CANCELLED');
                        alert('Đã hủy hóa đơn thành công!');
                        loadAndDisplayInvoices(); // Refresh danh sách
                    } catch (error) {
                        console.error('Error cancelling invoice:', error);
                        alert('Lỗi khi hủy hóa đơn. Vui lòng thử lại sau.');
                    }
                }
            });
        });
    };
    
    // Hiển thị danh sách thanh toán trong bảng
    const displayPayments = (payments, tableBodyElement) => {
        tableBodyElement.innerHTML = '';
        
        if (payments.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="9" class="text-center">Không có dữ liệu</td>';
            tableBodyElement.appendChild(row);
            return;
        }
        
        payments.forEach(payment => {
            const row = document.createElement('tr');
            
            const paymentDate = formatDate(payment.paymentDate);
            
            row.innerHTML = `
                <td>${payment.id}</td>
                <td>${payment.customerName}</td>
                <td>${payment.roomNumber}</td>
                <td>${payment.monthYear}</td>
                <td>${formatCurrency(payment.amount)}</td>
                <td>${paymentDate}</td>
                <td>${payment.paymentMethod}</td>
                <td>${payment.transactionId || '-'}</td>
                <td>
                    <button class="btn btn-sm btn-info view-payment" data-id="${payment.id}">
                        <i class="bi bi-eye me-1"></i> Xem
                    </button>
                    <button class="btn btn-sm btn-secondary print-receipt" data-id="${payment.id}">
                        <i class="bi bi-printer me-1"></i> In biên lai
                    </button>
                </td>
            `;
            
            tableBodyElement.appendChild(row);
        });
        
        // Thêm sự kiện cho các nút trong bảng
        tableBodyElement.querySelectorAll('.view-payment').forEach(button => {
            button.addEventListener('click', async (e) => {
                const paymentId = e.target.dataset.id || e.target.closest('button').dataset.id;
                alert(`Xem chi tiết thanh toán #${paymentId}`);
                // Implement chi tiết thanh toán ở đây
            });
        });
        
        tableBodyElement.querySelectorAll('.print-receipt').forEach(button => {
            button.addEventListener('click', async (e) => {
                const paymentId = e.target.dataset.id || e.target.closest('button').dataset.id;
                alert(`In biên lai cho thanh toán #${paymentId}`);
                // Implement in biên lai ở đây
            });
        });
    };
    
    // Tạo báo cáo
    const generateReport = async (reportType, startDate, endDate) => {
        try {
            const report = await API.getReports(reportType, startDate, endDate);
            displayReport(report);
        } catch (error) {
            console.error('Error generating report:', error);
            alert('Lỗi khi tạo báo cáo. Vui lòng thử lại sau.');
        }
    };
    
    // Hiển thị báo cáo
    const displayReport = (report) => {
        const reportResult = document.getElementById('reportResult');
        
        if (!report) {
            reportResult.innerHTML = '<p class="text-center text-muted">Không có dữ liệu báo cáo.</p>';
            return;
        }
        
        let reportHTML = `
            <div class="alert alert-primary">
                <h5 class="mb-1">Báo cáo: ${report.reportType}</h5>
                <p class="mb-0">Thời gian: ${report.period}</p>
            </div>
            
            <div class="row mb-4">
                <div class="col-md-3">
                    <div class="card bg-primary text-white">
                        <div class="card-body">
                            <h6 class="card-title">Tổng thu</h6>
                            <h3 class="mb-0">${formatCurrency(report.totalIncome)}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card bg-warning text-dark">
                        <div class="card-body">
                            <h6 class="card-title">Chưa thanh toán</h6>
                            <h3 class="mb-0">${formatCurrency(report.totalPending)}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card bg-danger text-white">
                        <div class="card-body">
                            <h6 class="card-title">Quá hạn</h6>
                            <h3 class="mb-0">${formatCurrency(report.totalOverdue)}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card bg-success text-white">
                        <div class="card-body">
                            <h6 class="card-title">Tỷ lệ thanh toán</h6>
                            <h3 class="mb-0">${report.paidInvoices}/${report.totalInvoices} (${Math.round(report.paidInvoices / report.totalInvoices * 100)}%)</h3>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Biểu đồ thu nhập theo kỳ (nếu có)
        if (report.incomeByPeriod && Object.keys(report.incomeByPeriod).length > 0) {
            reportHTML += `
                <div class="card mb-4">
                    <div class="card-header bg-light">
                        <h6 class="mb-0">Thu nhập theo kỳ</h6>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Kỳ</th>
                                    <th>Thu nhập</th>
                                </tr>
                            </thead>
                            <tbody>
            `;
            
            for (const [period, income] of Object.entries(report.incomeByPeriod)) {
                reportHTML += `
                    <tr>
                        <td>${period}</td>
                        <td>${formatCurrency(income)}</td>
                    </tr>
                `;
            }
            
            reportHTML += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        }
        
        // Biểu đồ thu nhập theo phương thức thanh toán (nếu có)
        if (report.incomeByPaymentMethod && Object.keys(report.incomeByPaymentMethod).length > 0) {
            reportHTML += `
                <div class="card mb-4">
                    <div class="card-header bg-light">
                        <h6 class="mb-0">Thu nhập theo phương thức thanh toán</h6>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Phương thức</th>
                                    <th>Thu nhập</th>
                                </tr>
                            </thead>
                            <tbody>
            `;
            
            for (const [method, income] of Object.entries(report.incomeByPaymentMethod)) {
                let methodName = '';
                switch (method) {
                    case 'CASH': methodName = 'Tiền mặt'; break;
                    case 'BANK_TRANSFER': methodName = 'Chuyển khoản'; break;
                    case 'CREDIT_CARD': methodName = 'Thẻ tín dụng'; break;
                    case 'MOBILE_BANKING': methodName = 'Mobile Banking'; break;
                    default: methodName = 'Khác';
                }
                
                reportHTML += `
                    <tr>
                        <td>${methodName}</td>
                        <td>${formatCurrency(income)}</td>
                    </tr>
                `;
            }
            
            reportHTML += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        }
        
        // Top hóa đơn quá hạn (nếu có)
        if (report.topOverdueInvoices && report.topOverdueInvoices.length > 0) {
            reportHTML += `
                <div class="card">
                    <div class="card-header bg-light">
                        <h6 class="mb-0">Hóa đơn quá hạn cao nhất</h6>
                    </div>
                    <div class="card-body">
                        <table class="table table-bordered table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Mã hóa đơn</th>
                                    <th>Khách hàng</th>
                                    <th>Phòng</th>
                                    <th>Tháng</th>
                                    <th>Số tiền</th>
                                    <th>Hạn thanh toán</th>
                                </tr>
                            </thead>
                            <tbody>
            `;
            
            report.topOverdueInvoices.forEach(invoice => {
                reportHTML += `
                    <tr>
                        <td>${invoice.invoiceNumber}</td>
                        <td>${invoice.customerName}</td>
                        <td>${invoice.roomNumber}</td>
                        <td>${invoice.monthYear}</td>
                        <td>${formatCurrency(invoice.amount)}</td>
                        <td>${formatDate(invoice.dueDate)}</td>
                    </tr>
                `;
            });
            
            reportHTML += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        }
        
        reportResult.innerHTML = reportHTML;
    };
    
    // Load và hiển thị danh sách hóa đơn
    const loadAndDisplayInvoices = async (status = null) => {
        const invoices = await loadInvoices(status);
        displayInvoices(invoices, document.getElementById('invoiceList'));
    };
    
    // Load và hiển thị danh sách thanh toán
    const loadAndDisplayPayments = async () => {
        const payments = await loadPayments();
        displayPayments(payments, document.getElementById('paymentList'));
    };
    
    // ===== EVENT HANDLERS =====
    
    // === Tab Hóa đơn ===
    
    // Load danh sách hóa đơn khi chọn tab
    document.querySelector('a[href="#invoices"]').addEventListener('click', () => {
        loadAndDisplayInvoices();
    });
    
    // Lọc hóa đơn theo trạng thái
    const invoiceStatusFilter = document.getElementById('invoiceStatusFilter');
    if (invoiceStatusFilter) {
        invoiceStatusFilter.addEventListener('change', (e) => {
            const status = e.target.value === 'all' ? null : e.target.value;
            loadAndDisplayInvoices(status);
        });
    }
    
    // Tìm kiếm hóa đơn
    const searchInvoiceBtn = document.getElementById('searchInvoiceBtn');
    if (searchInvoiceBtn) {
        searchInvoiceBtn.addEventListener('click', async () => {
            const searchTerm = document.getElementById('invoiceSearch').value.trim();
            if (!searchTerm) {
                loadAndDisplayInvoices();
                return;
            }
            
            try {
                const invoices = await API.searchInvoices(searchTerm);
                displayInvoices(invoices, document.getElementById('invoiceList'));
            } catch (error) {
                console.error('Error searching invoices:', error);
                alert('Lỗi khi tìm kiếm hóa đơn. Vui lòng thử lại sau.');
            }
        });
    }
    
    // === Tab Thanh toán ===
    
    // Load danh sách thanh toán khi chọn tab
    document.querySelector('a[href="#payments"]').addEventListener('click', () => {
        loadAndDisplayPayments();
    });
    
    // Lọc thanh toán theo phương thức
    const paymentMethodFilter = document.getElementById('paymentMethodFilter');
    if (paymentMethodFilter) {
        paymentMethodFilter.addEventListener('change', async (e) => {
            const method = e.target.value;
            
            try {
                let payments;
                
                if (method === 'all') {
                    payments = await API.getAllPayments();
                } else {
                    payments = await API.getPaymentsByMethod(method);
                }
                
                displayPayments(payments, document.getElementById('paymentList'));
            } catch (error) {
                console.error('Error filtering payments:', error);
                alert('Lỗi khi lọc thanh toán. Vui lòng thử lại sau.');
            }
        });
    }
    
    // Tìm kiếm thanh toán
    const searchPaymentBtn = document.getElementById('searchPaymentBtn');
    if (searchPaymentBtn) {
        searchPaymentBtn.addEventListener('click', async () => {
            const searchTerm = document.getElementById('paymentSearch').value.trim();
            if (!searchTerm) {
                loadAndDisplayPayments();
                return;
            }
            
            try {
                const payments = await API.searchPayments(searchTerm);
                displayPayments(payments, document.getElementById('paymentList'));
            } catch (error) {
                console.error('Error searching payments:', error);
                alert('Lỗi khi tìm kiếm thanh toán. Vui lòng thử lại sau.');
            }
        });
    }
    
    // === Tab Báo cáo ===
    
    // Tạo báo cáo
    const generateReportBtn = document.getElementById('generateReport');
    if (generateReportBtn) {
        generateReportBtn.addEventListener('click', () => {
            const reportType = document.getElementById('reportType').value;
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            
            if (!startDate || !endDate) {
                alert('Vui lòng chọn ngày bắt đầu và kết thúc');
                return;
            }
            
            generateReport(reportType, startDate, endDate);
        });
    }
    
    // === Modal tạo hóa đơn mới ===
    
    // Tìm kiếm khách hàng trong modal tạo hóa đơn
    const searchInvoiceCustomerBtn = document.getElementById('searchInvoiceCustomerBtn');
    if (searchInvoiceCustomerBtn) {
        searchInvoiceCustomerBtn.addEventListener('click', async () => {
            const searchTerm = document.getElementById('invoiceCustomerSearch').value.trim();
            if (!searchTerm) {
                alert('Vui lòng nhập CMND/CCCD hoặc số điện thoại để tìm kiếm');
                return;
            }
            
            const customerId = await searchCustomer(searchTerm, 
                document.getElementById('invoiceCustomerName'),
                document.getElementById('invoiceCustomerId')
            );
            
            if (customerId) {
                // Load danh sách phòng của khách hàng
                const hasRooms = await loadCustomerRooms(customerId, document.getElementById('invoiceRoomId'));
                
                if (!hasRooms) {
                    // Xóa trắng các trường thông tin phòng
                    document.getElementById('invoiceRoomNumber').value = '';
                    document.getElementById('invoiceMonthlyPrice').value = '';
                }
            }
        });
    }
    
    // Chọn phòng trong modal tạo hóa đơn
    const invoiceRoomIdSelect = document.getElementById('invoiceRoomId');
    if (invoiceRoomIdSelect) {
        invoiceRoomIdSelect.addEventListener('change', (e) => {
            const selectedOption = e.target.options[e.target.selectedIndex];
            if (!selectedOption.value) return;
            
            document.getElementById('invoiceRoomNumber').value = selectedOption.dataset.roomNumber;
            document.getElementById('invoiceMonthlyPrice').value = formatCurrency(selectedOption.dataset.price);
            
            // Điền giá trị mặc định vào trường số tiền
            document.getElementById('invoiceAmount').value = selectedOption.dataset.price;
            
            // Thiết lập tháng mặc định là tháng hiện tại
            const today = new Date();
            const monthYear = `${String(today.getMonth() + 1).padStart(2, '0')}/${today.getFullYear()}`;
            document.getElementById('invoiceMonth').value = monthYear;
            
            // Thiết lập hạn thanh toán mặc định là 15 ngày sau
            const dueDate = new Date();
            dueDate.setDate(dueDate.getDate() + 15);
            document.getElementById('invoiceDueDate').value = dueDate.toISOString().split('T')[0];
        });
    }
    
    // Xử lý tạo hóa đơn mới
    const submitInvoiceBtn = document.getElementById('submitInvoice');
    if (submitInvoiceBtn) {
        submitInvoiceBtn.addEventListener('click', async () => {
            // Lấy dữ liệu từ form
            const customerId = document.getElementById('invoiceCustomerId').value;
            const roomId = document.getElementById('invoiceRoomId').value;
            const monthYear = document.getElementById('invoiceMonth').value;
            const amount = document.getElementById('invoiceAmount').value;
            const dueDate = document.getElementById('invoiceDueDate').value;
            const description = document.getElementById('invoiceDescription').value;
            
            if (!customerId || !roomId || !monthYear || !amount || !dueDate) {
                alert('Vui lòng điền đầy đủ thông tin bắt buộc');
                return;
            }
            
            // Tạo hóa đơn mới
            const invoiceData = {
                customerId,
                roomId,
                monthYear,
                amount,
                dueDate,
                description
            };
            
            const newInvoice = await createInvoice(invoiceData);
            
            if (newInvoice) {
                alert('Tạo hóa đơn thành công!');
                
                // Đóng modal và reload trang
                const modal = bootstrap.Modal.getInstance(document.getElementById('newInvoiceModal'));
                modal.hide();
                
                loadAndDisplayInvoices();
            }
        });
    }
    
    // === Modal thanh toán hóa đơn ===
    
    // Xử lý thanh toán hóa đơn
    const submitPaymentBtn = document.getElementById('submitPayment');
    if (submitPaymentBtn) {
        submitPaymentBtn.addEventListener('click', async () => {
            // Lấy dữ liệu từ form
            const invoiceId = document.getElementById('payInvoiceId').value;
            const paymentMethod = document.getElementById('payMethod').value;
            const transactionId = document.getElementById('payTransactionId').value;
            const notes = document.getElementById('payNotes').value;
            
            if (!invoiceId || !paymentMethod) {
                alert('Vui lòng chọn phương thức thanh toán');
                return;
            }
            
            // Thanh toán hóa đơn
            const payment = await payInvoice(invoiceId, paymentMethod, transactionId, notes);
            
            if (payment) {
                alert('Thanh toán hóa đơn thành công!');
                
                // Đóng modal và reload trang
                const modal = bootstrap.Modal.getInstance(document.getElementById('payInvoiceModal'));
                modal.hide();
                
                loadAndDisplayInvoices();
            }
        });
    }
    
    // === Modal tạo thanh toán mới ===
    
    // Tìm kiếm khách hàng trong modal tạo thanh toán
    const searchDirectPaymentCustomerBtn = document.getElementById('searchDirectPaymentCustomerBtn');
    if (searchDirectPaymentCustomerBtn) {
        searchDirectPaymentCustomerBtn.addEventListener('click', async () => {
            const searchTerm = document.getElementById('directPaymentCustomerSearch').value.trim();
            if (!searchTerm) {
                alert('Vui lòng nhập CMND/CCCD hoặc số điện thoại để tìm kiếm');
                return;
            }
            
            const customerId = await searchCustomer(searchTerm, 
                document.getElementById('directPaymentCustomerName'),
                document.getElementById('directPaymentCustomerId')
            );
            
            if (customerId) {
                // Load danh sách phòng của khách hàng
                const hasRooms = await loadCustomerRooms(customerId, document.getElementById('directPaymentRoomId'));
                
                if (!hasRooms) {
                    // Xóa trắng các trường thông tin phòng
                    document.getElementById('directPaymentRoomNumber').value = '';
                }
            }
        });
    }
    
    // Chọn phòng trong modal tạo thanh toán
    const directPaymentRoomIdSelect = document.getElementById('directPaymentRoomId');
    if (directPaymentRoomIdSelect) {
        directPaymentRoomIdSelect.addEventListener('change', async (e) => {
            const selectedOption = e.target.options[e.target.selectedIndex];
            if (!selectedOption.value) return;
            
            document.getElementById('directPaymentRoomNumber').value = selectedOption.dataset.roomNumber;
            
            // Load danh sách hóa đơn chưa thanh toán cho phòng này
            const customerId = document.getElementById('directPaymentCustomerId').value;
            const roomId = selectedOption.value;
            
            await loadPendingInvoices(customerId, roomId, document.getElementById('directPaymentInvoiceId'));
            
            // Thiết lập tháng mặc định là tháng hiện tại
            const today = new Date();
            const monthYear = `${String(today.getMonth() + 1).padStart(2, '0')}/${today.getFullYear()}`;
            document.getElementById('directPaymentMonth').value = monthYear;
        });
    }
    
    // Chọn hóa đơn trong modal tạo thanh toán
    const directPaymentInvoiceIdSelect = document.getElementById('directPaymentInvoiceId');
    if (directPaymentInvoiceIdSelect) {
        directPaymentInvoiceIdSelect.addEventListener('change', (e) => {
            const selectedOption = e.target.options[e.target.selectedIndex];
            if (!selectedOption.value) return;
            
            // Điền số tiền từ hóa đơn
            document.getElementById('directPaymentAmount').value = selectedOption.dataset.amount;
        });
    }
    
    // Xử lý tạo thanh toán mới
    const submitDirectPaymentBtn = document.getElementById('submitDirectPayment');
    if (submitDirectPaymentBtn) {
        submitDirectPaymentBtn.addEventListener('click', async () => {
            // Lấy dữ liệu từ form
            const customerId = document.getElementById('directPaymentCustomerId').value;
            const roomId = document.getElementById('directPaymentRoomId').value;
            const invoiceId = document.getElementById('directPaymentInvoiceId').value || null;
            const monthYear = document.getElementById('directPaymentMonth').value;
            const amount = document.getElementById('directPaymentAmount').value;
            const paymentMethod = document.getElementById('directPaymentMethod').value;
            const transactionId = document.getElementById('directPaymentTransactionId').value;
            const notes = document.getElementById('directPaymentNotes').value;
            
            if (!customerId || !roomId || !monthYear || !amount || !paymentMethod) {
                alert('Vui lòng điền đầy đủ thông tin bắt buộc');
                return;
            }
            
            if (invoiceId) {
                // Thanh toán hóa đơn hiện có
                const payment = await payInvoice(invoiceId, paymentMethod, transactionId, notes);
                
                if (payment) {
                    alert('Thanh toán hóa đơn thành công!');
                    
                    // Đóng modal và reload trang
                    const modal = bootstrap.Modal.getInstance(document.getElementById('newPaymentModal'));
                    modal.hide();
                    
                    loadAndDisplayPayments();
                }
            } else {
                // Tạo thanh toán mới (không qua hóa đơn)
                const paymentData = {
                    customerId,
                    roomId,
                    monthYear,
                    amount,
                    paymentMethod,
                    transactionId,
                    notes
                };
                
                const newPayment = await createPayment(paymentData);
                
                if (newPayment) {
                    alert('Tạo thanh toán thành công!');
                    
                    // Đóng modal và reload trang
                    const modal = bootstrap.Modal.getInstance(document.getElementById('newPaymentModal'));
                    modal.hide();
                    
                    loadAndDisplayPayments();
                }
            }
        });
    }
    
    // ===== INITIALIZATION =====
    
    // Load dữ liệu ban đầu khi trang được tải
    loadAndDisplayInvoices();
    
    // Thiết lập ngày mặc định cho báo cáo
    const today = new Date();
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    
    document.getElementById('startDate').value = firstDayOfMonth.toISOString().split('T')[0];
    document.getElementById('endDate').value = lastDayOfMonth.toISOString().split('T')[0];
    
    // Load thống kê nhanh
    const loadQuickStats = async () => {
        try {
            // Tổng thu tháng này
            const monthYear = `${String(today.getMonth() + 1).padStart(2, '0')}/${today.getFullYear()}`;
            const monthlyIncome = await API.getTotalMonthlyIncome(monthYear);
            document.getElementById('monthly-income').textContent = formatCurrency(monthlyIncome);
            
            // Hóa đơn chưa thanh toán
            const pendingInvoices = await API.getInvoicesByStatus('PENDING');
            const totalPending = pendingInvoices.reduce((total, invoice) => total + parseFloat(invoice.amount), 0);
            document.getElementById('unpaid-invoices').textContent = formatCurrency(totalPending);
            
            // Phòng đã thuê
            const rooms = await API.getAllRooms();
            const occupiedRooms = rooms.filter(room => room.status === 'OCCUPIED').length;
            document.getElementById('occupied-rooms-count').textContent = `${occupiedRooms}/${rooms.length}`;
            
            // Hóa đơn quá hạn
            const overdueInvoices = await API.getOverdueInvoices();
            document.getElementById('overdue-invoices').textContent = overdueInvoices.length;
        } catch (error) {
            console.error('Error loading quick stats:', error);
        }
    };
    
    loadQuickStats();
});