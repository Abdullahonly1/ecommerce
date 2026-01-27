document.addEventListener('DOMContentLoaded', function () {

    const mainImg = document.querySelector('.product-main-img');

    // ✅ Safety check
    if (!mainImg) {
        return;
    }

    // Improve UX
    mainImg.style.cursor = 'zoom-in';

    mainImg.addEventListener('click', function () {

        const isZoomed = this.classList.toggle('zoomed');

        // Cursor feedback
        this.style.cursor = isZoomed ? 'zoom-out' : 'zoom-in';

    });

});
