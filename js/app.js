const scrollButton = document.querySelector('.js-scroll-button');
const letsGo = document.getElementById('letsgo');
const body = document.getElementsByTagName('body')[0];

scrollButton.addEventListener('click', e => {
  $('html, body').animate({
    scrollTop: $("#letsgo").position().top
}, 750);
});
