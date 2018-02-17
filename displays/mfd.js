/*

*/

/**
 * NoSleep.js v0.5.0
 * Rich Tibbett
 * git.io/vfn01
 * MIT license
 */
(function(f){function e(b,c,a){var d=document.createElement("source");d.src=a;d.type="video/"+c;b.appendChild(d)}var b=/Android/ig.test(navigator.userAgent),c=/AppleWebKit/.test(navigator.userAgent)&&/Mobile\/\w+/.test(navigator.userAgent),a=function(){c?this.noSleepTimer=null:b&&(this.noSleepVideo=document.createElement("video"),this.noSleepVideo.setAttribute("loop",""),e(this.noSleepVideo,"webm","data:video/webm;base64,GkXfo0AgQoaBAUL3gQFC8oEEQvOBCEKCQAR3ZWJtQoeBAkKFgQIYU4BnQI0VSalmQCgq17FAAw9CQE2AQAZ3aGFtbXlXQUAGd2hhbW15RIlACECPQAAAAAAAFlSua0AxrkAu14EBY8WBAZyBACK1nEADdW5khkAFVl9WUDglhohAA1ZQOIOBAeBABrCBCLqBCB9DtnVAIueBAKNAHIEAAIAwAQCdASoIAAgAAUAmJaQAA3AA/vz0AAA="),
e(this.noSleepVideo,"mp4","data:video/mp4;base64,AAAAHGZ0eXBpc29tAAACAGlzb21pc28ybXA0MQAAAAhmcmVlAAAAG21kYXQAAAGzABAHAAABthADAowdbb9/AAAC6W1vb3YAAABsbXZoZAAAAAB8JbCAfCWwgAAAA+gAAAAAAAEAAAEAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIAAAIVdHJhawAAAFx0a2hkAAAAD3wlsIB8JbCAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAIAAAACAAAAAABsW1kaWEAAAAgbWRoZAAAAAB8JbCAfCWwgAAAA+gAAAAAVcQAAAAAAC1oZGxyAAAAAAAAAAB2aWRlAAAAAAAAAAAAAAAAVmlkZW9IYW5kbGVyAAAAAVxtaW5mAAAAFHZtaGQAAAABAAAAAAAAAAAAAAAkZGluZgAAABxkcmVmAAAAAAAAAAEAAAAMdXJsIAAAAAEAAAEcc3RibAAAALhzdHNkAAAAAAAAAAEAAACobXA0dgAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAIAAgASAAAAEgAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABj//wAAAFJlc2RzAAAAAANEAAEABDwgEQAAAAADDUAAAAAABS0AAAGwAQAAAbWJEwAAAQAAAAEgAMSNiB9FAEQBFGMAAAGyTGF2YzUyLjg3LjQGAQIAAAAYc3R0cwAAAAAAAAABAAAAAQAAAAAAAAAcc3RzYwAAAAAAAAABAAAAAQAAAAEAAAABAAAAFHN0c3oAAAAAAAAAEwAAAAEAAAAUc3RjbwAAAAAAAAABAAAALAAAAGB1ZHRhAAAAWG1ldGEAAAAAAAAAIWhkbHIAAAAAAAAAAG1kaXJhcHBsAAAAAAAAAAAAAAAAK2lsc3QAAAAjqXRvbwAAABtkYXRhAAAAAQAAAABMYXZmNTIuNzguMw=="));
return this};a.prototype.enable=function(a){c?(this.disable(),this.noSleepTimer=window.setInterval(function(){window.location.href="/";window.setTimeout(window.stop,0)},a||15E3)):b&&this.noSleepVideo.play()};a.prototype.disable=function(){c?this.noSleepTimer&&(window.clearInterval(this.noSleepTimer),this.noSleepTimer=null):b&&this.noSleepVideo.pause()};f.NoSleep=a})(this);

/**
 * padStart polyfill
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/padStart
 */

if (!String.prototype.padStart) {
    String.prototype.padStart = function padStart(targetLength,padString) {
        targetLength = targetLength>>0; //floor if number or convert non-number to 0;
        padString = String(padString || ' ');
        if (this.length > targetLength) {
            return String(this);
        }
        else {
            targetLength = targetLength-this.length;
            if (targetLength > padString.length) {
                padString += padString.repeat(targetLength/padString.length);
            }
            return padString.slice(0,targetLength) + String(this);
        }
    };
}




/**
 * Multi-Function Display utils and API
 * skhmt, 2017
 * github.com/skhmt
 */

let mfd = (function () {

	let m = {};
	let q = [];
	let processing = false;
	
	const _apiPath = '/mfd/api';
	const _cryptoPath = '/mfd/crypto';
	const _displaysPath = '/mfd/displays';

	function queue(fn) {
		q.push(fn);
		if (!processing) {
			processing = true;
			process();
		}
	}

	function process() {
		if (q.length > 0) {
			let fetcher = q.shift();
			if (fetcher.action === 'wait') {
				setTimeout(process, fetcher.data);
			}
			else {
				const time = Date.now();
				let options = `{"action":"${fetcher.action}", "data":"${fetcher.data}"}`;
				const iv = m.crypto.generateIVHex();
				const cryptoText = m.crypto.encrypt(options, iv);
				const body = iv + ':' + cryptoText;

				fetch(_apiPath, {method: 'POST', body})
				.then(res => {
					if (!res.ok) {
						process(); // keep going
						throw 'error'
					}
					else if (res.status == 200) { // there's a response
						if (fetcher.action == 'capture') return res.blob();
						return res.json();
					}
					else {
						process(); // status 204, OK but no response
						throw 'handled'
					}
				})
				.then(data => {
					if (fetcher.callback) fetcher.callback(data);
					process();
				}, reason => {/*not doing anything besides catching the "handled" error*/});
			}
		}
		else processing = false;
	}
	
	m.getVersion = function (fn) {
		queue({
	        action: 'version',
	        callback: json => {
	            if (typeof fn === 'function') fn(json);
	        }
	    });
	};

	m.getCrypto = function (fn) {
		fetch(_cryptoPath)
        .then(res => {
            if (res.ok) return res.json();
        })
        .then(json => {
            m.crypto.salt = json.salt;
            m.crypto.iterations = json.iterations;
            if (typeof fn === 'function') fn();
        });
	};

    m.getDisplays = function (fn) {
        fetch(_displaysPath)
        .then(res => res.json())
        .then(json => {
            if (typeof fn === 'function') fn(json.displays);
        });
    };

	m.test = function (fn) {
	    queue({
	        action: 'test',
	        callback: json => {
	            if (typeof fn === 'function') fn(json);
	        }
	    });
	}

	m.ping = function (fn) {
	    const start = Date.now();
	    queue({
            action: 'test',
            callback: json => {
                const ping = Date.now() - start;
                if (typeof fn === 'function') {
                    fn(ping);
                }
            }
        });
	}
	
	m.wait = function(time) {
		if (typeof time !== 'number')  console.error('Bad input, use mfd.wait(ms to wait)');
		else queue({action: 'wait', data: time});
		return this;
	}
	m.w = m.wait;

	m.keyTap = function (key, modifiers) {
	    if (typeof key !== 'string') {
	        console.error('Bad input, use mfd.keyTap(key[, modifier array])');
	        return this;
	    }
		if (modifiers) {
			if (modifiers.includes('shift')) m.keyOn('shift');
			if (modifiers.includes('ctrl')) m.keyOn('ctrl');
			if (modifiers.includes('alt')) m.keyOn('alt');
			if (modifiers.includes('ralt')) m.keyOn('ralt');
			if (modifiers.includes('win') || modifiers.includes('windows')) m.keyOn('windows');
			if (modifiers.includes('cmd') || modifiers.includes('command')) m.keyOn('command');
		}
		m.keyOn(key).keyOff(key);
		if (modifiers) {
			if (modifiers.includes('shift')) m.keyOff('shift');
            if (modifiers.includes('ctrl')) m.keyOff('ctrl');
            if (modifiers.includes('alt')) m.keyOff('alt');
            if (modifiers.includes('ralt')) m.keyOff('ralt');
            if (modifiers.includes('win') || modifiers.includes('windows')) m.keyOff('windows');
            if (modifiers.includes('cmd') || modifiers.includes('command')) m.keyOff('command');
		}
		return this;
	};
	m.kT = m.keyTap;

	m.keyOn = function (key) {
		if (typeof key !== 'string') console.error('Bad input, use mfd.keyOn(key)');
		else queue({action: 'keyon', data: key});
		return this;
	};
	m.kD = m.keyOn;

	m.keyOff = function (key) {
		if (typeof key !== 'string') console.error('Bad input, use mfd.keyOff(key)');
        else queue({action: 'keyoff', data: key});
		return this;
	};
	m.kU = m.keyOff;

	m.typeString = function (str) {
	    if (typeof str !== 'string') console.error('Bad input, use mfd.typeString(string)');
        else {
			const newStr = str.replace(/\"/g, '\\\"').replace(/\\/g, '\\\\');
			queue({action: 'typestring', data: newStr});
		}
		return this;
	};
	m.ts = m.typeString;

	m.mouseMove = function (x, y) {
	    if (typeof x !== 'number' || typeof y !== 'number') {
	        console.error('Bad input, use mfd.mouseMove(x, y)');
	    }
	    else queue({action: 'mousemove', data: `${x},${y}`});
		return this;
	};
	m.mM = m.mouseMOve;
	
	m.mouseClick = function (button) {
	    if (typeof button !== 'string' && typeof button !== 'number') console.error('Bad input, use mfd.mouseClick(button)');
	    else {
	        m.mouseOn(button).mouseOff(button);
	    }
		return this;
	};
	m.mC = m.mouseClick;

	// utility function for buttons
	function getBtn(button) {
		// assuming 'button' is a string
		switch(button) {
			case 'right':
			case 'mouse2':
			case '2':
			case 2:
				return 'right';
			case 'center':
			case 'middle':
			case 'mouse3':
			case '3':
			case 3:
				return 'center';
			default:
				return 'left';
		}
	}
	
	m.mouseOn = function (button) {
	    if (typeof button !== 'string' && typeof button !== 'number') console.error('Bad input, use mfd.mouseOn(button)');
	    else {
	        queue({action: 'mouseon', data: getBtn(button)});
	    }
		return this;
	};
	m.mD = m.mouseOn;

	m.mouseOff = function (button) {
	    if (typeof button !== 'string' && typeof button !== 'number') console.error('Bad input, use mfd.mouseOff(button)');
	    else {
	        queue({action: 'mouseoff', data: getBtn(button)});
	    }
		return this;
	};
	m.mU = m.mouseOff;

	m.mouseWheel = function (clicks) {
	    if (typeof click !== 'number') console.error('Bad input, use mfd.mouseWheel(clicks)');
		else queue({action: 'mousewheel', data: clicks});
		return this;
	};
	m.mW = m.mouseWheel;

	m.getMouse = function (fn) {
		queue({
			action: 'mousepointer',
			callback: json => {
				if (typeof fn === 'function') fn(json);
			}
		});
	};
	m.gM = m.getMouse;

	m.exec = function (cmd) {
	    if (typeof cmd !== 'string') console.error('Bad input, use mfd.exec(cmd)');
	    else queue({action: 'exec', data: cmd});
		return this;
	};

	m.url = function (url) {
	    if (typeof url !== 'string') console.error('Bad input, use mfd.url(url)');
	    else queue({action: 'url', data: url});
		return this;
	}

	m.getPixel = function (x, y, fn) {
	    if (typeof x !== 'number' || typeof y !== 'number') console.error('Bad input, use mfd.getPixel(x, y[, fn])');
	    else queue({
	        action: 'pixel',
	        data: `${x},${y}`,
	        callback: json => {
                if (typeof fn === 'function') fn(json);
            }
        });
	};
	m.gP = m.getPixel;

    // ex: mfd.getScreenshot(0,0,2560,1440, url => window.open(url))
	m.getScreenshot = function (x, y, width, height, fn) {
	    if (typeof x !== 'number' || typeof y !== 'number' || typeof width !== 'number' || typeof height !== 'number') {
	        console.error('Bad input, use mfd.getScreenshot(x, y, width, height[, fn])');
	    }
	    else queue({
	        action: 'capture',
	        data: `${x},${y},${width},${height}`,
	        callback: blob => {
                if (typeof fn === 'function') fn(URL.createObjectURL(blob));
            }
        });
	};
	m.gS = m.getScreenshot;

    // vJoy api, mfd.vj.[...]()

    m.vJoy = {};

    m.vJoy.info = function (fn) {
        queue({
            action: 'vj_info',
            callback: json => {
                if (typeof fn === 'function') {
                    fn(json);
                }
            }
        });
    };

    m.vJoy.reset = function () {
        queue({action: 'vj_resetall'});
    };

    m.vJoy.device = function (rID) {
        if (typeof rID !== 'number') console.error('Bad input, use mfd.vJoy.device(rID)');
        let dev = {};

        dev.info = function (fn) {
            queue({
               action: 'vj_vjd_info',
               data: rID,
               callback: json => {
                   if (typeof fn === 'function') {
                       fn(json);
                   }
               }
            });
        };

        dev.acquire = function () {
            queue({action: 'vj_vjd_acquire', data: rID});
        };

        dev.relinquish = function () {
            queue({action: 'vj_vjd_relinquish', data: rID});
        };

        dev.setAxis = function (axis, value) {
            if (typeof axis !== 'string' || typeof value !== 'number') {
                console.error('Bad input, use mfd.vJoy.device(rID).setAxis(axis, value)');
            }
            else {
                queue({action: 'vj_vjd_setaxis', data: `${rID},${axis},${value|0}`});
            }
        };

        dev.setBtn = function (btn, value) {
            if (typeof btn !== 'number' || typeof value !== 'boolean') {
                console.error('Bad input, use mfd.vJoy.device(rID).setBtn(btn, value)');
            }
            else {
                queue({action: 'vj_vjd_setbtn', data: `${rID},${btn|0},${value}`});
            }
        };

        dev.setPovDiscrete = function (nPov, value) {
            if (typeof nPov !== 'number' || typeof value !== 'number') {
                console.error('Bad input, use mfd.vJoy.device(rID).setPovDiscrete(npov, value)');
            }
            else {
                queue({action: 'vj_vjd_setdiscpov', data: `${rID},${nPov|0},${value|0}`});
            }
        };

        dev.setPovContinuous = function (nPov, value) {
            if (typeof nPov !== 'number' || typeof value !== 'number') {
                console.error('Bad input, use mfd.vJoy.device(rID).setPovContinuous(npov, value)');
            }
            else {
                queue({action: 'vj_vjd_setcontpov', data: `${rID},${nPov|0},${value|0}`});
            }
        };

        dev.reset = function () {
            queue({action: 'vj_vjd_reset', data: `${rID}`});
        };

        dev.resetBtns = function () {
            queue({action: 'vj_vjd_resetbtns', data: `${rID}`});
        };

        dev.resetPovs = function () {
            queue({action: 'vj_vjd_resetpovs', data: `${rID}`});
        };

        return dev;
    }

    m.changePass = function (newPass, fn) {
        if (typeof newPass !== 'string') console.error('Bad input, use mfd.changePass(newPassword[, fn])')
        else {
            queue({
                action: 'changepass',
                data: newPass,
                callback: function (res) {
                     m.crypto.salt = res.salt;
                     m.crypto.iterations = res.iterations;
                     m.crypto.generateKey(newPass);
                     if (typeof fn === 'function') fn();
                } // close callback
            }); // close queue
        }
        return this;
    }

	m.crypto = (function () {
		let c = {};
		
		c.stringToHex = function (str) {
			let hex = '';
			for (let i = 0; i < str.length; i++) {
				hex += '' + str.charCodeAt(i).toString(16);
			}
			return hex;
		}
		c.hexToString = function (hex) {
			const bytes = asmCrypto.hex_to_bytes(hex);
			return asmCrypto.bytes_to_string(bytes);
		}
	
		c.salt; // hex
		c.key; // hex
		c.keyByteArray;
		c.iterations = 10000;
		c.keyLength = 16;
		c.id = '';
	
		c.encrypt = function (plainText, iv) {
			if (typeof iv == 'string') iv = asmCrypto.hex_to_bytes(iv);
			if (typeof plainText == 'string') plainText = asmCrypto.string_to_bytes(plainText);
			if (!c.keyByteArray) c.keyByteArray = asmCrypto.hex_to_bytes(c.key);
			return asmCrypto.bytes_to_hex(  asmCrypto.AES_GCM.encrypt(plainText, c.keyByteArray, iv, 'mfd-1.0.0', 16)  ); // add adata and tagsize
		};
		c.decrypt = function (cipherText, iv) {
			if (typeof iv == 'string') iv = asmCrypto.hex_to_bytes(iv);
			if (typeof cipherText == 'string') cipherText = asmCrypto.hex_to_bytes(cipherText);
			if (!c.keyByteArray) c.keyByteArray = asmCrypto.hex_to_bytes(c.key);
			return asmCrypto.bytes_to_hex(  asmCrypto.AES_GCM.decrypt(cipherText, c.keyByteArray, iv, 'mfd-1.0.0', 16)  );
		};
		c.generateKey = function (pass) {
			if (typeof pass == 'string') pass = asmCrypto.string_to_bytes(pass);
			const saltByteArray = asmCrypto.hex_to_bytes(c.salt);
			c.setKey( asmCrypto.PBKDF2_HMAC_SHA256.hex( pass, saltByteArray, c.iterations, c.keyLength ) );
			return c.key;
		};
		c.setKey = function (hexKey) {
			c.key = hexKey;
			c.keyByteArray = asmCrypto.hex_to_bytes(c.key);
		}
		c.generateIVHex = function () {
		    // 12 bytes, first 4 are "user id", last 8 is timestamp
		    if (!c.id) {
		        const hex = '0123456789abcdef';
		        let id = '';
		        for(let h = 0; h < 8; h++) {
		            id += hex[Math.random()*16|0];
		        }
		        c.id = id;
		    }
		    const time = Date.now().toString(16).padStart(16, '0');
            return c.id + time;
		};
	
		return c;
	})();

	return m;
})();