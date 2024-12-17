let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/refresh', function(message) {
            console.log('Received refresh signal');
            window.location.reload();
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

// Connect when the page loads
document.addEventListener('DOMContentLoaded', function() {
    connect();
});

// Disconnect when the page unloads
window.addEventListener('beforeunload', function() {
    disconnect();
});
