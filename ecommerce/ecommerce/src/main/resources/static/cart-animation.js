document.addEventListener('DOMContentLoaded', function () {

    const cartBadge = document.getElementById('cart-badge');

    // ✅ Prevent JS error if badge not present
    if (!cartBadge) {
        return;
    }

    function updateCartCount() {
        fetch('/cart/count')
            .then(res => {
                if (!res.ok) return 0;
                return res.json();
            })
            .then(count => {
                if (count > 0) {
                    cartBadge.textContent = count;
                    cartBadge.style.display = 'inline-block';
                    cartBadge.style.animation = 'pulse 0.6s ease';
                } else {
                    cartBadge.style.display = 'none';
                }
            })
            .catch(() => {
                cartBadge.style.display = 'none';
            });
    }

    // Initial load
    updateCartCount();

    // ✅ Works with form submit buttons
    document.querySelectorAll('form[action^="/cart/add"]').forEach(form => {
        form.addEventListener('submit', () => {
            setTimeout(updateCartCount, 800);
        });
    });

});
