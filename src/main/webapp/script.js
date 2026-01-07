function showScreen(screenId) {
    document.querySelectorAll('.screen-content').forEach(screen => {
        screen.classList.add('hidden');
    });
    document.getElementById(screenId).classList.remove('hidden');
    
    if (screenId === 'mainMenuScreen' || screenId === 'withdrawScreen' || screenId === 'depositScreen' || screenId === 'transferScreen') {
        updateBalance();
    }
    
    if (screenId === 'historyScreen') {
        displayTransactionHistory();
    }
    
    clearMessages();
    clearInputs(screenId);
}

function clearInputs(screenId) {
    if (screenId === 'changePinScreen') {
        document.getElementById('currentPin').value = '';
        document.getElementById('newPin').value = '';
        document.getElementById('confirmNewPin').value = '';
    }
    if (screenId === 'transferScreen') {
        document.getElementById('recipientAccount').value = '';
        document.getElementById('transferAmount').value = '';
        document.getElementById('transferPin').value = '';
    }
}

function clearMessages() {
    document.getElementById('createMessage').innerHTML = '';
    document.getElementById('loginMessage').innerHTML = '';
    document.getElementById('withdrawMessage').innerHTML = '';
    document.getElementById('depositMessage').innerHTML = '';
    document.getElementById('transferMessage').innerHTML = '';
    document.getElementById('changePinMessage').innerHTML = '';
}

function showMessage(elementId, message, isError = false) {
    const messageClass = isError ? 'message-error' : 'message-success';
    document.getElementById(elementId).innerHTML = `<div class="message ${messageClass}">${message}</div>`;
}

function createAccount() {
    const name = document.getElementById('newName').value.trim();
    const accountNumber = document.getElementById('newAccountNumber').value.trim();
    const pin = document.getElementById('newPassword').value.trim();
    const balance = document.getElementById('initialDeposit').value.trim();

    if (!name || !accountNumber || !pin || !balance) {
        showMessage('createMessage', 'Please fill in all fields', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("accountNo", accountNumber);
    formData.append("name", name);
    formData.append("pin", pin);
    formData.append("balance", balance);

    fetch("createAccount", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
    .then(res => res.text())
    .then(data => {
        data = data.trim();
        console.log("CreateAccount response:", data);

        if (data === "SUCCESS") {
            showMessage('createMessage', 'Account created successfully!');
            setTimeout(() => showScreen('loginScreen'), 1000);
        } else if (data === "EXISTS") {
            showMessage('createMessage', 'Account already exists', true);
        } else {
            showMessage('createMessage', 'Server error', true);
        }
    })
    .catch(err => {
        console.error(err);
        showMessage('createMessage', 'Server error', true);
    });
}

function login() {
    const accountNumber = document.getElementById('accountNumber').value.trim();
    const password = document.getElementById('password').value;

    if (!accountNumber || !password) {
        showMessage('loginMessage', 'Please enter account number and PIN', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("accountNo", accountNumber);
    formData.append("pin", password);

    fetch("login", {
        method: "POST",
        body: formData
    })
    .then(res => res.text())
	.then(data => {
	    data = data.trim(); // safety

	    if (data === "success") {
	        document.getElementById('accountNumber').value = '';
	        document.getElementById('password').value = '';
	        showScreen('mainMenuScreen');
			loadUserName(); 
			updateBalance(); // REAL DB BALANCE

	    } else if (data === "invalid") {
	        showMessage('loginMessage', 'Invalid account number or PIN', true);
	    } else {
	        showMessage('loginMessage', 'Server error', true);
	    }
	});

}

function updateBalance() {
    fetch("getBalance")
        .then(res => res.text())
        .then(data => {
            data = data.trim();

            if (data === "NO_SESSION") {
                showScreen('loginScreen');
                return;
            }

            if (data === "ERROR" || data === "NOT_FOUND") {
                console.error("Balance fetch error:", data);
                return;
            }

            document.getElementById('currentBalance').textContent = data;
            document.getElementById('withdrawBalance').textContent = data;
            document.getElementById('depositBalance').textContent = data;
            document.getElementById('transferBalance').textContent = data;
        });
}

function withdraw() {
    const amount = document.getElementById('withdrawAmount').value.trim();

    if (!amount || isNaN(amount) || amount <= 0) {
        showMessage('withdrawMessage', 'Enter a valid amount', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("amount", amount);

    fetch("withdraw", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
    .then(res => res.text())
    .then(data => {
        data = data.trim();

        if (data === "SUCCESS") {
            showMessage('withdrawMessage', 'Withdrawal successful');
            updateBalance(); // 🔥 real DB refresh
            setTimeout(() => showScreen('mainMenuScreen'), 1200);
        }
        else if (data === "INSUFFICIENT") {
            showMessage('withdrawMessage', 'Insufficient balance', true);
        }
        else if (data === "NO_SESSION") {
            showScreen('loginScreen');
        }
        else {
            showMessage('withdrawMessage', 'Server error', true);
        }
    });
}

function deposit() {
    const amount = document.getElementById('depositAmount').value.trim();

    if (!amount || isNaN(amount) || amount <= 0) {
        showMessage('depositMessage', 'Enter a valid amount', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("amount", amount);

    fetch("deposit", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
    .then(res => res.text())
    .then(data => {
        data = data.trim();

        if (data === "SUCCESS") {
            showMessage('depositMessage', 'Deposit successful');
            updateBalance(); // 🔥 refresh from DB
            setTimeout(() => showScreen('mainMenuScreen'), 1200);
        }
        else if (data === "NO_SESSION") {
            showScreen('loginScreen');
        }
        else {
            showMessage('depositMessage', 'Server error', true);
        }
    });
}

function displayTransactionHistory() {
    fetch("transactions")
        .then(res => res.text())
        .then(data => {
            if (data === "NO_SESSION") {
                showScreen('loginScreen');
                return;
            }

            const historyContainer = document.getElementById('transactionHistory');
            const transactions = JSON.parse(data);

            if (transactions.length === 0) {
                historyContainer.innerHTML =
                    '<p style="color:#a0aec0;text-align:center;">No transactions yet</p>';
                return;
            }

            let html = "";

            transactions.forEach(tx => {
                let typeClass = "deposit";
                if (tx.type === "WITHDRAW") typeClass = "withdraw";
                if (tx.type === "DEPOSIT") typeClass = "deposit";

                html += `
                    <div class="transaction-item ${typeClass}">
                        <div>
                            <div class="transaction-type">${tx.type}</div>
                            <div style="font-size:12px;color:#a0aec0;">${tx.date}</div>
                        </div>
                        <div class="transaction-amount">$${tx.amount}</div>
                    </div>
                `;
            });

            historyContainer.innerHTML = html;
        });
}

function transferMoney() {
    const toAccount = document.getElementById('recipientAccount').value.trim();
    const amount = document.getElementById('transferAmount').value.trim();

    if (!toAccount || !amount || isNaN(amount) || amount <= 0) {
        showMessage('transferMessage', 'Enter valid details', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("toAccount", toAccount);
    formData.append("amount", amount);

    fetch("transfer", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
    .then(res => res.text())
    .then(data => {
        data = data.trim();

        if (data === "SUCCESS") {
            showMessage('transferMessage', 'Transfer successful');
            updateBalance();
            setTimeout(() => showScreen('mainMenuScreen'), 1500);
        }
        else if (data === "INSUFFICIENT") {
            showMessage('transferMessage', 'Insufficient balance', true);
        }
        else if (data === "RECEIVER_NOT_FOUND") {
            showMessage('transferMessage', 'Recipient not found', true);
        }
        else if (data === "SAME_ACCOUNT") {
            showMessage('transferMessage', 'Cannot transfer to same account', true);
        }
        else {
            showMessage('transferMessage', 'Server error', true);
        }
    });
}

function changePin() {
    const currentPin = document.getElementById('currentPin').value.trim();
    const newPin = document.getElementById('newPin').value.trim();
    const confirmPin = document.getElementById('confirmNewPin').value.trim();

    if (!currentPin || !newPin || newPin !== confirmPin) {
        showMessage('changePinMessage', 'Invalid input', true);
        return;
    }

    const formData = new URLSearchParams();
    formData.append("currentPin", currentPin);
    formData.append("newPin", newPin);

    fetch("changePin", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData.toString()
    })
    .then(res => res.text())
    .then(data => {
        data = data.trim();

        if (data === "SUCCESS") {
            showMessage('changePinMessage', 'PIN changed successfully');
            setTimeout(() => showScreen('mainMenuScreen'), 1200);
        }
        else if (data === "WRONG_PIN") {
            showMessage('changePinMessage', 'Current PIN is incorrect', true);
        }
        else {
            showMessage('changePinMessage', 'Server error', true);
        }
    });
}

function logout() {
    fetch("logout")
        .finally(() => {
            showScreen('welcomeScreen');
        });
}


function loadUserName() {
    fetch("getUser")
        .then(res => res.text())
        .then(name => {
            name = name.trim();

            if (name !== "NO_SESSION") {
                document.getElementById("userName").textContent = name;
            }
        });
}

