const jsimages = Array.from(document.querySelectorAll('.js-grow-image'));

jsimages.forEach(el => {
  el.addEventListener('click', e => {
    console.log('.modal[name="' + el.getAttribute('target') + '"]');
    const modal = document.querySelector('.modal[name="' + el.getAttribute('target') + '"]')
    modal.classList.toggle('active');
    modal.addEventListener("click", e => {
      modal.classList.remove('active');
    });
  });
});
