
const $start = document.getElementById('start');
const $enter = document.getElementById('enter');
const $display = document.getElementById('display');
const $keyfield = document.getElementById('keyfield');

$keyfield.style.display = 'none';
mfd.getCrypto(() => {
    const storedSalt = localStorage.getItem('mfd-salt');
    const storedIts = localStorage.getItem('mfd-its');
    if (storedSalt == mfd.crypto.salt && storedIts == mfd.crypto.iterations) {
        mfd.crypto.setKey(localStorage.getItem('mfd-key'));
        mfd.test(res => {
            if (!res.test || res.test !== 'OK') {
                $keyfield.style.display = 'inline';
                $enter.innerHTML = 'Enter Key';
            }
        })
    } else {
        $keyfield.style.display = 'inline';
        $enter.innerHTML = 'Enter Key';
    }
});

function submit () {
    if ($keyfield.style.display !== 'none') {
        mfd.crypto.generateKey($keyfield.value);
        
        localStorage.setItem('mfd-salt', mfd.crypto.salt);
        localStorage.setItem('mfd-its', mfd.crypto.iterations);
        mfd.test(res => {
            if (res.test && res.test === 'OK') {
                localStorage.setItem('mfd-key', mfd.crypto.key);
                
                login();
            }
            else {
                return;
            }
        });
    }
    else { // credentials already stored
        login();
    }

    function login () {
        // making it full screen
        let rfs = $display.requestFullscreen || $display.webkitRequestFullScreen || $display.mozRequestFullScreen;
        if (rfs) rfs.call($display);

        // switching out button for the grid
        $start.style.display = 'none';
        $display.style.display = 'grid';
        
        // animating button fade-in
        const buttons = $display.children;
        const fadeInStagger = 50; // in ms
        for (let i=0; i < buttons.length; i++) {
            setTimeout(() => buttons[i].style.opacity = 1, i*fadeInStagger);
        }
    
        // enabling NoSleep if it exists
        if (NoSleep) {
            let nosleep = new NoSleep();
            nosleep.enable();
        }
    }

    return false; // prevent default form post action
}

navigator.vibrate = navigator.vibrate || navigator.webkitVibrate || navigator.mozVibrate || navigator.msVibrate;
function vib () {
    if (navigator.vibrate) navigator.vibrate(50);
}