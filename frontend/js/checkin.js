// js/checkin.js
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
    const searchCustomer = async (searchTerm, customerInfoContainer) => {
        try {
            const customers = await API.searchCustomers(searchTerm);
            
            if (customers && customers.length > 0) {
                const customer = customers[0]; // Lấy kết quả đầu tiên
                
                // Điền thông tin vào form
                document.getElementById('fullName').value = customer.fullName;
                document.getElementById('identificationNumber').value = customer.identificationNumber;
                document.getElementById('phoneNumber').value = customer.phoneNumber;
                document.getElementById('email').value = customer.email;
                
                return customer;
            } else {
                // Không tìm thấy khách hàng, xóa trắng các trường để người dùng có thể nhập mới
                document.getElementById('fullName').value = '';
                document.getElementById('identificationNumber').value = '';
                document.getElementById('phoneNumber').value = '';
                document.getElementById('email').value = '';
                
                alert('Không tìm thấy khách hàng với thông tin này. Vui lòng nhập thông tin mới.');
                return null;
            }
        } catch (error) {
            console.error('Error searching customer:', error);
            alert('Lỗi khi tìm kiếm khách hàng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tìm kiếm khách hàng cho đặt phòng
    const searchBookingCustomer = async (searchTerm) => {
        try {
            const customers = await API.searchCustomers(searchTerm);
            
            if (customers && customers.length > 0) {
                const customer = customers[0]; // Lấy kết quả đầu tiên
                
                // Điền thông tin vào form
                document.getElementById('bookingFullName').value = customer.fullName;
                document.getElementById('bookingIdentificationNumber').value = customer.identificationNumber;
                document.getElementById('bookingPhoneNumber').value = customer.phoneNumber;
                document.getElementById('bookingEmail').value = customer.email;
                
                return customer;
            } else {
                alert('Không tìm thấy khách hàng với thông tin này. Vui lòng nhập thông tin mới.');
                document.getElementById('newCustomerCheck').checked = true;
                
                // Xóa trắng các trường thông tin
                document.getElementById('bookingFullName').value = '';
                document.getElementById('bookingIdentificationNumber').value = '';
                document.getElementById('bookingPhoneNumber').value = '';
                document.getElementById('bookingEmail').value = '';
                
                // Bỏ disabled để người dùng có thể nhập thông tin mới
                enableBookingCustomerFields();
                
                return null;
            }
        } catch (error) {
            console.error('Error searching customer for booking:', error);
            alert('Lỗi khi tìm kiếm khách hàng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tạo khách hàng mới
    const createCustomer = async (customerData) => {
        try {
            const newCustomer = await API.createCustomer(customerData);
            return newCustomer;
        } catch (error) {
            console.error('Error creating customer:', error);
            alert('Lỗi khi tạo khách hàng mới. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Load danh sách phòng trống
    const loadAvailableRooms = async (selectElement) => {
        try {
            const rooms = await API.getAvailableRooms();
            
            selectElement.innerHTML = '<option value="" selected disabled>-- Chọn phòng --</option>';
            
            if (rooms && rooms.length > 0) {
                rooms.forEach(room => {
                    const option = document.createElement('option');
                    option.value = room.id;
                    option.textContent = `${room.roomNumber} - ${room.roomTypeName} - ${formatCurrency(room.monthlyPrice)}`;
                    option.dataset.roomNumber = room.roomNumber;
                    option.dataset.roomType = room.roomTypeName;
                    option.dataset.price = room.monthlyPrice;
                    selectElement.appendChild(option);
                });
                return true;
            } else {
                alert('Không có phòng trống nào khả dụng.');
                return false;
            }
        } catch (error) {
            console.error('Error loading available rooms:', error);
            alert('Lỗi khi tải danh sách phòng trống. Vui lòng thử lại sau.');
            return false;
        }
    };
    
    // Tạo đặt phòng mới
    const createBooking = async (bookingData) => {
        try {
            const newBooking = await API.createBooking(bookingData);
            return newBooking;
        } catch (error) {
            console.error('Error creating booking:', error);
            alert('Lỗi khi đặt phòng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tạo check-in mới
    const createCheckIn = async (checkInData) => {
        try {
            const newCheckIn = await API.createCheckIn(checkInData);
            return newCheckIn;
        } catch (error) {
            console.error('Error creating check-in:', error);
            alert('Lỗi khi nhận phòng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tạo check-in từ booking
    const createCheckInFromBooking = async (bookingId) => {
        try {
            const checkIn = await API.createCheckInFromBooking(bookingId);
            return checkIn;
        } catch (error) {
            console.error('Error creating check-in from booking:', error);
            alert('Lỗi khi nhận phòng từ đặt phòng. Vui lòng thử lại sau.');
            return null;
        }
    };
    
    // Tìm kiếm đặt phòng
    const searchBookings = async (searchTerm) => {
        try {
            const bookings = await API.searchBookings(searchTerm);
            return bookings;
        } catch (error) {
            console.error('Error searching bookings:', error);
            alert('Lỗi khi tìm kiếm đặt phòng. Vui lòng thử lại sau.');
            return [];
        }
    };
    
    // Load danh sách đặt phòng
    const loadBookings = async () => {
        try {
            const bookings = await API.getAllBookings();
            return bookings;
        } catch (error) {
            console.error('Error loading bookings:', error);
            alert('Lỗi khi tải danh sách đặt phòng. Vui lòng thử lại sau.');
            return [];
        }
    };
    
    // Hiển thị danh sách đặt phòng trong bảng
    const displayBookings = (bookings, tableBodyElement) => {
        tableBodyElement.innerHTML = '';
        
        if (bookings.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="7" class="text-center">Không có dữ liệu</td>';
            tableBodyElement.appendChild(row);
            return;
        }
        
        bookings.forEach(booking => {
            const row = document.createElement('tr');
            
            const statusClass = 
                booking.status === 'PENDING' ? 'bg-warning text-dark' : 
                booking.status === 'CHECKED_IN' ? 'bg-success text-white' : 
                'bg-secondary text-white';
            
            const statusText = 
                booking.status === 'PENDING' ? 'Chờ nhận phòng' : 
                booking.status === 'CHECKED_IN' ? 'Đã nhận phòng' : 
                'Đã hủy';
            
            // Format dates
            const bookingDate = booking.bookingDate ? formatDate(booking.bookingDate) : '';
            const checkInDate = booking.checkInDate ? formatDate(booking.checkInDate) : '';
            
            row.innerHTML = `
                <td>${booking.id}</td>
                <td>${booking.customerName || ''}</td>
                <td>${booking.roomNumber || ''}</td>
                <td>${bookingDate}</td>
                <td>${checkInDate}</td>
                <td><span class="badge ${statusClass}">${statusText}</span></td>
                <td>
                    ${booking.status === 'PENDING' ? 
                        `<button class="btn btn-sm btn-success check-in-booking" data-id="${booking.id}">
                            <i class="bi bi-box-arrow-in-right me-1"></i> Nhận phòng
                        </button>` : 
                        ''
                    }
                    <button class="btn btn-sm btn-info view-booking" data-id="${booking.id}">
                        <i class="bi bi-eye me-1"></i> Xem
                    </button>
                    ${booking.status === 'PENDING' ? 
                        `<button class="btn btn-sm btn-danger cancel-booking" data-id="${booking.id}">
                            <i class="bi bi-x-circle me-1"></i> Hủy
                        </button>` : 
                        ''
                    }
                </td>
            `;
            
            tableBodyElement.appendChild(row);
        });
        
        // Thêm sự kiện cho các nút trong bảng
        addBookingButtonHandlers(tableBodyElement);
    };
    
    // Thêm sự kiện cho các nút trong bảng đặt phòng
    const addBookingButtonHandlers = (tableBodyElement) => {
        // Nút nhận phòng
        tableBodyElement.querySelectorAll('.check-in-booking').forEach(button => {
            button.addEventListener('click', async (e) => {
                const bookingId = e.target.dataset.id || e.target.closest('button').dataset.id;
                
                if (confirm(`Bạn có chắc chắn muốn nhận phòng cho đặt phòng #${bookingId}?`)) {
                    const checkIn = await createCheckInFromBooking(bookingId);
                    
                    if (checkIn) {
                        alert('Nhận phòng thành công!');
                        loadAndDisplayBookings();
                        loadAndDisplayBookingCheckIns();
                    }
                }
            });
        });
        
        // Nút xem chi tiết
        tableBodyElement.querySelectorAll('.view-booking').forEach(button => {
            button.addEventListener('click', async (e) => {
                const bookingId = e.target.dataset.id || e.target.closest('button').dataset.id;
                
                try {
                    const booking = await API.getBookingById(bookingId);
                    alert(`Thông tin đặt phòng #${bookingId}\n\nKhách hàng: ${booking.customerName}\nPhòng: ${booking.roomNumber}\nNgày đặt: ${formatDate(booking.bookingDate)}\nNgày nhận phòng: ${formatDate(booking.checkInDate)}\nTrạng thái: ${booking.status === 'PENDING' ? 'Chờ nhận phòng' : booking.status === 'CHECKED_IN' ? 'Đã nhận phòng' : 'Đã hủy'}\nGhi chú: ${booking.notes || 'Không có'}`);
                } catch (error) {
                    console.error('Error viewing booking:', error);
                    alert('Lỗi khi xem thông tin đặt phòng. Vui lòng thử lại sau.');
                }
            });
        });
        
        // Nút hủy đặt phòng
        tableBodyElement.querySelectorAll('.cancel-booking').forEach(button => {
            button.addEventListener('click', async (e) => {
                const bookingId = e.target.dataset.id || e.target.closest('button').dataset.id;
                
                if (confirm(`Bạn có chắc chắn muốn hủy đặt phòng #${bookingId}?`)) {
                    try {
                        await API.updateBookingStatus(bookingId, 'CANCELLED');
                        alert('Đã hủy đặt phòng thành công!');
                        loadAndDisplayBookings();
                    } catch (error) {
                        console.error('Error cancelling booking:', error);
                        alert('Lỗi khi hủy đặt phòng. Vui lòng thử lại sau.');
                    }
                }
            });
        });
    };
    
    // Enable/disable các trường thông tin khách hàng trong modal đặt phòng
    const enableBookingCustomerFields = () => {
        document.getElementById('bookingFullName').removeAttribute('disabled');
        document.getElementById('bookingIdentificationNumber').removeAttribute('disabled');
        document.getElementById('bookingPhoneNumber').removeAttribute('disabled');
        document.getElementById('bookingEmail').removeAttribute('disabled');
    };
    
    const disableBookingCustomerFields = () => {
        document.getElementById('bookingFullName').setAttribute('disabled', 'disabled');
        document.getElementById('bookingIdentificationNumber').setAttribute('disabled', 'disabled');
        document.getElementById('bookingPhoneNumber').setAttribute('disabled', 'disabled');
        document.getElementById('bookingEmail').setAttribute('disabled', 'disabled');
    };
    
    // Load và hiển thị danh sách đặt phòng
    const loadAndDisplayBookings = async () => {
        const bookings = await loadBookings();
        
        // Hiển thị trong tab danh sách đặt phòng
        displayBookings(bookings, document.getElementById('bookingTableList'));
    };
    
    // Load và hiển thị danh sách đặt phòng chờ nhận phòng
    const loadAndDisplayBookingCheckIns = async () => {
        const pendingBookings = await API.getBookingsByStatus('PENDING');
        
        // Hiển thị trong tab nhận phòng đã đặt
        displayBookings(pendingBookings, document.getElementById('bookingCheckInList'));
    };
    
    // ===== EVENT HANDLERS =====
    
    // === Tab Nhận phòng mới ===
    
    // Tìm kiếm khách hàng
    document.getElementById('searchCustomerBtn').addEventListener('click', () => {
        const searchTerm = document.getElementById('customerSearch').value.trim();
        if (!searchTerm) {
            alert('Vui lòng nhập CMND/CCCD hoặc số điện thoại để tìm kiếm');
            return;
        }
        
        searchCustomer(searchTerm, document.getElementById('customerInfo'));
    });
    
    // Khi nhập trong ô tìm kiếm khách hàng
    document.getElementById('customerSearch').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            document.getElementById('searchCustomerBtn').click();
        }
    });
    
    // Load danh sách phòng trống khi trang được tải
    loadAvailableRooms(document.getElementById('availableRooms'));
    
    // Khi chọn phòng
    document.getElementById('availableRooms').addEventListener('change', (e) => {
        const selectedOption = e.target.options[e.target.selectedIndex];
        if (!selectedOption.value) return;
        
        document.getElementById('roomNumber').value = selectedOption.dataset.roomNumber;
        document.getElementById('roomType').value = selectedOption.dataset.roomType;
        document.getElementById('monthlyPrice').value = formatCurrency(selectedOption.dataset.price);
        
        // Thiết lập ngày nhận phòng mặc định là hôm nay
        const today = new Date();
        document.getElementById('checkInDate').value = today.toISOString().split('T')[0];
    });
    
    // Xử lý nhận phòng mới
    document.getElementById('submitCheckIn').addEventListener('click', async () => {
        // Lấy thông tin khách hàng
        const fullName = document.getElementById('fullName').value.trim();
        const identificationNumber = document.getElementById('identificationNumber').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();
        const email = document.getElementById('email').value.trim();
        
        // Lấy thông tin phòng
        const roomId = document.getElementById('availableRooms').value;
        const checkInDate = document.getElementById('checkInDate').value;
        
        // Kiểm tra dữ liệu đầu vào
        if (!fullName || !identificationNumber || !phoneNumber || !roomId || !checkInDate) {
            alert('Vui lòng điền đầy đủ thông tin bắt buộc');
            return;
        }
        
        // Tạo hoặc lấy khách hàng
        let customerId;
        
        // Kiểm tra xem khách hàng đã tồn tại hay chưa
        try {
            const customers = await API.searchCustomers(identificationNumber);
            
            if (customers && customers.length > 0) {
                // Khách hàng đã tồn tại
                customerId = customers[0].id;
            } else {
                // Tạo khách hàng mới
                const customerData = {
                    fullName,
                    identificationNumber,
                    phoneNumber,
                    email
                };
                
                const newCustomer = await createCustomer(customerData);
                if (!newCustomer) return;
                
                customerId = newCustomer.id;
            }
        } catch (error) {
            console.error('Error checking customer:', error);
            alert('Lỗi khi kiểm tra thông tin khách hàng. Vui lòng thử lại sau.');
            return;
        }
        
        // Tạo check-in mới
        const checkInData = {
            customerId,
            roomId,
            checkInDate
        };
        
        const newCheckIn = await createCheckIn(checkInData);
        
        if (newCheckIn) {
            alert('Nhận phòng thành công!');
            
            // Reset form
            document.getElementById('customerSearch').value = '';
            document.getElementById('fullName').value = '';
            document.getElementById('identificationNumber').value = '';
            document.getElementById('phoneNumber').value = '';
            document.getElementById('email').value = '';
            document.getElementById('availableRooms').value = '';
            document.getElementById('roomNumber').value = '';
            document.getElementById('roomType').value = '';
            document.getElementById('monthlyPrice').value = '';
            document.getElementById('checkInDate').value = '';
            
            // Reload danh sách phòng trống
            loadAvailableRooms(document.getElementById('availableRooms'));
        }
    });
    
    // === Tab Nhận phòng đã đặt ===
    
    // Tìm kiếm đặt phòng
    document.getElementById('searchBookingBtn').addEventListener('click', async () => {
        const searchTerm = document.getElementById('bookingSearch').value.trim();
        if (!searchTerm) {
            // Nếu không có từ khóa, load tất cả đặt phòng đang chờ
            loadAndDisplayBookingCheckIns();
            return;
        }
        
        const bookings = await searchBookings(searchTerm);
        
        // Lọc chỉ lấy các đặt phòng đang chờ nhận
        const pendingBookings = bookings.filter(booking => booking.status === 'PENDING');
        
        displayBookings(pendingBookings, document.getElementById('bookingCheckInList'));
    });
    
    // Khi nhập trong ô tìm kiếm đặt phòng
    document.getElementById('bookingSearch').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            document.getElementById('searchBookingBtn').click();
        }
    });
    
    // === Tab Danh sách đặt phòng ===
    
    // Load danh sách đặt phòng khi chọn tab
    document.querySelector('a[href="#bookingList"]').addEventListener('click', () => {
        loadAndDisplayBookings();
    });
    
    // === Modal đặt phòng mới ===
    
    // Khi checkbox khách hàng mới được chọn/bỏ chọn
    document.getElementById('newCustomerCheck').addEventListener('change', (e) => {
        if (e.target.checked) {
            // Cho phép nhập thông tin mới
            enableBookingCustomerFields();
            
            // Xóa thông tin hiện tại
            document.getElementById('bookingFullName').value = '';
            document.getElementById('bookingIdentificationNumber').value = '';
            document.getElementById('bookingPhoneNumber').value = '';
            document.getElementById('bookingEmail').value = '';
        } else {
            // Khóa các trường thông tin và yêu cầu tìm kiếm khách hàng
            disableBookingCustomerFields();
            
            // Xóa thông tin hiện tại
            document.getElementById('bookingFullName').value = '';
            document.getElementById('bookingIdentificationNumber').value = '';
            document.getElementById('bookingPhoneNumber').value = '';
            document.getElementById('bookingEmail').value = '';
        }
    });
    
    // Tìm kiếm khách hàng cho đặt phòng
    document.getElementById('searchBookingCustomerBtn').addEventListener('click', () => {
        const searchTerm = document.getElementById('bookingCustomerSearch').value.trim();
        if (!searchTerm) {
            alert('Vui lòng nhập CMND/CCCD hoặc số điện thoại để tìm kiếm');
            return;
        }
        
        searchBookingCustomer(searchTerm);
    });
    
    // Khi nhập trong ô tìm kiếm khách hàng trong modal đặt phòng
    document.getElementById('bookingCustomerSearch').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            document.getElementById('searchBookingCustomerBtn').click();
        }
    });
    
    // Khi mở modal đặt phòng mới
    document.getElementById('newBookingModal').addEventListener('show.bs.modal', () => {
        // Reset form
        document.getElementById('bookingCustomerSearch').value = '';
        document.getElementById('newCustomerCheck').checked = false;
        document.getElementById('bookingFullName').value = '';
        document.getElementById('bookingIdentificationNumber').value = '';
        document.getElementById('bookingPhoneNumber').value = '';
        document.getElementById('bookingEmail').value = '';
        
        disableBookingCustomerFields();
        
        // Load danh sách phòng trống
        loadAvailableRooms(document.getElementById('bookingAvailableRooms'));
        
        // Thiết lập ngày nhận phòng mặc định là ngày mai
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('bookingCheckInDate').value = tomorrow.toISOString().split('T')[0];
        
        document.getElementById('bookingNotes').value = '';
    });
    
    // Xử lý đặt phòng mới
    document.getElementById('submitBooking').addEventListener('click', async () => {
        // Lấy thông tin khách hàng
        const fullName = document.getElementById('bookingFullName').value.trim();
        const identificationNumber = document.getElementById('bookingIdentificationNumber').value.trim();
        const phoneNumber = document.getElementById('bookingPhoneNumber').value.trim();
        const email = document.getElementById('bookingEmail').value.trim();
        
        // Lấy thông tin đặt phòng
        const roomId = document.getElementById('bookingAvailableRooms').value;
        const checkInDate = document.getElementById('bookingCheckInDate').value;
        const notes = document.getElementById('bookingNotes').value.trim();
        
        // Kiểm tra dữ liệu đầu vào
        if (!fullName || !identificationNumber || !phoneNumber || !roomId || !checkInDate) {
            alert('Vui lòng điền đầy đủ thông tin bắt buộc');
            return;
        }
        
        // Tạo hoặc lấy khách hàng
        let customerId;
        const isNewCustomer = document.getElementById('newCustomerCheck').checked;
        
        try {
            if (isNewCustomer) {
                // Tạo khách hàng mới
                const customerData = {
                    fullName,
                    identificationNumber,
                    phoneNumber,
                    email
                };
                
                const newCustomer = await createCustomer(customerData);
                if (!newCustomer) return;
                
                customerId = newCustomer.id;
            } else {
                // Kiểm tra xem khách hàng đã tồn tại chưa
                const customers = await API.searchCustomers(identificationNumber);
                
                if (customers && customers.length > 0) {
                    customerId = customers[0].id;
                } else {
                    alert('Không tìm thấy thông tin khách hàng. Vui lòng tìm kiếm lại hoặc chọn "Khách hàng mới".');
                    return;
                }
            }
        } catch (error) {
            console.error('Error handling customer for booking:', error);
            alert('Lỗi khi xử lý thông tin khách hàng. Vui lòng thử lại sau.');
            return;
        }
        
        // Tạo đặt phòng mới
        const bookingData = {
            customerId,
            roomId,
            checkInDate,
            notes
        };
        
        const newBooking = await createBooking(bookingData);
        
        if (newBooking) {
            alert('Đặt phòng thành công!');
            
            // Đóng modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('newBookingModal'));
            modal.hide();
            
            // Reload danh sách đặt phòng
            loadAndDisplayBookings();
        }
    });
    
    // ===== INITIALIZATION =====
    
    // Load danh sách đặt phòng chờ nhận phòng khi trang được tải
    loadAndDisplayBookingCheckIns();
    
    // Thiết lập ngày nhận phòng mặc định là hôm nay
    const today = new Date();
    document.getElementById('checkInDate').value = today.toISOString().split('T')[0];
});