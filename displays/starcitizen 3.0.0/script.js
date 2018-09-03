
const $start = document.getElementById('start');
const $enter = document.getElementById('enter');
const $display = document.getElementById('display');
const $keyfield = document.getElementById('keyfield');
const $tabcontent = document.getElementById('tabcontent');
const $tabs = document.getElementById('tabs');

const contentChildren = $tabcontent.children;
const tabChildren = $tabs.children;

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
    return false; // prevent default form post action
}

function login () {
    // making it full screen
    let rfs = $display.requestFullscreen || $display.webkitRequestFullScreen || $display.mozRequestFullScreen;
    if (rfs) rfs.call($display);

    // switching out button for the grid
    $start.style.display = 'none';
    $display.style.display = 'block';
	
    // animating button fade-in	
    const fadeStagger = 50; // in ms
	for (let i=0; i < tabChildren.length; i++) {
		setTimeout(() => tabChildren[i].style.opacity = 1, i*fadeStagger);
    }

    // enabling NoSleep if it exists
    if (NoSleep) {
        let nosleep = new NoSleep();
        nosleep.enable();
    }

    setTab('tab1');
}

navigator.vibrate = navigator.vibrate || navigator.webkitVibrate || navigator.mozVibrate || navigator.msVibrate;
function vib () {
    if (navigator.vibrate) navigator.vibrate(50);
}

function setTab(tabName) {
    for (let t = 0; t < tabChildren.length; t++) {
        const tab = tabChildren[t];
        if (tab.id === tabName) tab.className = 'activeTab';
        else tab.className = 'inactiveTab';
    }

    const fadeStagger = 32;

    const displayName = tabName + '_display';
    for (let c = 0; c < contentChildren.length; c++) {
        const disp = contentChildren[c];
        const dispButtons = disp.children;
        if (disp.id === displayName) {
            disp.style.display = 'grid';
            for (let d = 0; d < dispButtons.length; d++) {
                setTimeout(() => dispButtons[d].style.opacity = 1, d*fadeStagger)
            }
        }
        else {
            disp.style.display = 'none';
            for (let d = 0; d < dispButtons.length; d++) {
                dispButtons[d].style.opacity = 0;
            }
        }
    }
}