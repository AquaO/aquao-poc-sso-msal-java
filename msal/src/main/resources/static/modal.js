var modal = document.getElementById("welcomeModal");
var span = document.getElementsByClassName("close")[0];
window.onload = function() {
    if (userName !== null) {
        modal.style.display = "block";
    }
}
span.onclick = function() {
    modal.style.display = "none";
}
window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}