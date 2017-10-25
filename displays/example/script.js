
const $start = document.getElementById('start');
const $enter = document.getElementById('enter');
const $mfd = document.getElementById('mfd');
const $keyfield = document.getElementById('keyfield');

$keyfield.style.display = 'none';
mfd.getCrypto(() => {
    const storedSalt = localStorage.getItem('mfd-salt');
    const storedIts = localStorage.getItem('mfd-its');
    if (storedSalt == mfd.crypto.salt && storedIts == mfd.crypto.iterations) {
        mfd.crypto.setKey(localStorage.getItem('mfd-key'));
        mfd.test(keyIsGood => {
            if (keyIsGood) return;
            else {
                $keyfield.style.display = 'inline';
                $enter.innerHTML = 'Enter Key';
            }
        })
    } else {
        $keyfield.style.display = 'inline';
        $enter.innerHTML = 'Enter Key';
    }
});

$enter.addEventListener('click', () => {
    if ($keyfield.style.display !== 'none') {
        mfd.crypto.generateKey($keyfield.value);
        localStorage.setItem('mfd-key', mfd.crypto.key);
        localStorage.setItem('mfd-salt', mfd.crypto.salt);
        localStorage.setItem('mfd-its', mfd.crypto.iterations);
    }

    // making it full screen
    let rfs = $mfd.requestFullscreen || $mfd.webkitRequestFullScreen;
    rfs.call($mfd);

    // switching out button for the grid
    $start.style.display = 'none';
    $mfd.style.display = 'grid';

    // enabling NoSleep if it exists
    if (NoSleep) {
        let nosleep = new NoSleep();
        nosleep.enable();
    }
});

navigator.vibrate = navigator.vibrate || navigator.webkitVibrate || navigator.mozVibrate || navigator.msVibrate;

function vib () {
    if (navigator.vibrate) navigator.vibrate(50);
}