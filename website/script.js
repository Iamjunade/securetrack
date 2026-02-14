document.addEventListener('DOMContentLoaded', () => {

    // Smooth Scroll for Anchors
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    // Random Glitch Intensity
    const glitchText = document.querySelector('.glitch');
    if (glitchText) {
        setInterval(() => {
            const r = Math.random();
            if (r > 0.9) {
                glitchText.style.textShadow = '2px 0 red, -2px 0 blue';
            } else if (r > 0.95) {
                glitchText.style.textShadow = '4px 0 red, -4px 0 blue';
            } else {
                glitchText.style.textShadow = 'none';
            }
        }, 100);
    }

    // Dynamic Copyright Year
    const yearSpan = document.getElementById('year');
    if (yearSpan) {
        yearSpan.textContent = new Date().getFullYear();
    }
});
