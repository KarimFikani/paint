Paint
=====

This application does the following:
- Paint with your finger
- Clear the canvas but hitting the clear button
- The color button changes the color of all the lines that you drew to red
- Undo/Redo last drawings
- Rotates the canvas according to the device's sensors. The image that you draw will always be pointing upwards. For example if you drew an arrrow pointing upwards, and you have the phone placed on the table, and you rotate the phone on the z-axis you will be always see the arrow pointing upwards. You can turn ON/OFF this feature with the R-ON or R-OFF button. In order to see this feature behave properly, you need to turn on the feature first and then draw.

Improvements
============
- Make the canvas rotation smoother by interpolating the angle
- Speed up the drawing and make it asynchronous. A thread should be always drawing onto your display while your touching the screen. Currently once you touch the screen it draws which is synchronous 

