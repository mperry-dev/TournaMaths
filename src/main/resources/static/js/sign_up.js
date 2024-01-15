$(document).ready(function() {
    $('#signupForm').on('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        var userData = {
            firstname: $('#firstname').val(),
            lastname: $('#lastname').val(),
            email: $('#email').val(),
            password: $('#password').val()
        };

        $.ajax({
            url: '/register',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(userData),
            success: function(response) {
                console.log('Success:', response);
                // Handle success (e.g., display a success message or redirect)
                // Redirect to /create_questions on success
                window.location.href = '/create_questions';
            },
            error: function(error) {
                console.error('Error:', error);
                // Handle errors here
            }
        });
    });
});
