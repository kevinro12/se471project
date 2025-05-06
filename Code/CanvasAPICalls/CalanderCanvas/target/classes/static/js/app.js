//makes sure the script is only run once the HTML is fully loaded
document.addEventListener( 'DOMContentLoaded', () => {
    setupEventListeners();
});

//event listeners for buttons
function setupEventListeners() {
    document.getElementById('login-btn').addEventListener('click', facilitateLogin);
    document.getElementById('refresh-btn').addEventListener('click', refreshData);
    document.getElementById('calendar-btn').addEventListener('click', goToCalendar);
}

//This function handles the login button
function facilitateLogin() {
    const apiKey = prompt('Enter your Canvas API Key');
    if (apiKey) {
        //Sends the API key to the backend for validation
        fetch('/api/login', {
            method: 'POST', 
            headers: {'Content-Type': 'text/plain'},
            body: apiKey
        })
        .then(response => {
            if (response.ok) {
             alert('login successful');            
        } else {
            alert('No API key entered. Try again');
        }        
    })
    .catch(error => {
        console.error('Error during login:', error);
        alert('An error occured. Try again,');
        });    
    } else  {
    alert('No API key entered');
    }
}

//Navigate to Calendar page
function goToCalendar() {
    window.location.href = '/calendar.html'; //this redirects to the calendar page
}