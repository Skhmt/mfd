## Quick and dirty vJoy tutorial for MFD

Assuming you are already in the same context as other `mfd.` actions (you have crypto working, etc)...

1. `mfd.vJoy.info(callback)` to get useful information, like `existingDevices` and `enabled`
2. `mfd.vJoy.device(1).info(callback)` to get useful information on device 1 (use a different number for a different device of course) - basically everything in the returned object is important
3. `mfd.vJoy.device(1).acquire()` to take control of the device
4. Optionally `mfd.vJoy.device(1).info(callback)` again to see if the status changed to `VJD_STAT_OWN`, which it should be after using `acquire()`
5. `mfd.vJoy.device(1)` `.setBtn(...)` or `.setAxis(...)` or `.setPovDiscrete(...)` or `.setPovContinuous(...)` to set that device's inputs, based on information gained from `mfd.vJoy.device(1).info(callback)`
6. At any time, use `mfd.vJoy.device(1).reset()` or `.resetBtns()` or `.resetPovs()`
7. If you can, use `mfd.vJoy.device(1).relinquish()` on exit 

The following example assumes you're already authenticated with MFD and there is a vJoy virtual device:


	// Initialization code, assume it is called on page load


	let vd1;
	let vd1Info;

	mfd.vJoy.info(vji => {
		// simple error checks
		if (!vji.enabled) return console.error('vJoy not enabled');
		if (vji.existingDevices < 1) return console.error('No vJoy virtual devices found');
		
		vd1 = mfd.vJoy.device(1);
		
		vd1.info(x => {
			// acquiring the device
			if (x.status === "VJD_STAT_FREE") {
				vd1.acquire();
			}
			
			// setting vdInfo for use by the event handlers
			vd1Info = x;
		});
	});


	// Event handlers, assume button clicks (not included in this example) call one of the functions below


	// presses then releases button 1, 2, and 3 on virtual device 1
	function fireEverything() {
		if (vd1Info && vd1Info.btnNumber >= 3) {
			vd1.setBtn(1, true);
			vd1.setBtn(2, true);
			vd1.setBtn(3, true);
			vd1.setBtn(1, false);
			vd1.setBtn(2, false);
			vd1.setBtn(3, false);
		} else console.error('Insufficient buttons found or there was an error setting up vJoy');
	}

	// centers x axis and maxes y axis, ignores z, rx, ry, rz, sl0, sl1, and whl.
	function diveDive() {
		// if an axis' max is 0, the virtual device doesn't have that axis enabled
		if (vd1Info && vd1Info.y_max > 0 && vd1Info.x_max > 0) { 
			vd1.setAxis(y, vd1Info.y_max);
			vd1.setAxis(x, vd1Info.x_max/2);
		} else console.error('VD1 doesn't have an X or Y axis or there was an error setting up vJoy');
	}

	// looks left/west with POV hat 1
	function someoneOnTheWing() {
		if (vd1Info) {
			if (vd1Info.contPovNumber > 0) {
				vd1.setPovContinuous(1, 3/4*vd1Info.pov_max); // See API on why this number
			} else if (vd1Info.discPovNumber > 0) {
				vd1.setPovDiscrete(1, 3);
			} else {
				console.error('No POV hats found');
			}
		} else console.error('There was an error setting up vJoy');
	}

Because there is no way currently to get axis or button state information, you actually have to keep track of it yourself.
I might make a higher level vJoy API on top of the existing one to keep track of and change virtual device input state.
Or I might just leave that up to the individual to implement, which is definitely easier.