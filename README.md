# LG-Smart-TV-2012
DTH for control TV and Smart app to detect TV code and create device controller
A Device Handler to connect 2012-2013 LG Smart TV's to Smart Things

This is based on a similar project that connects Smartthings to Pre 2012 LG Smart TVs By dan06 and Samlalor.
 
 * -Modified feb, 2021 by Mariano Colmenarejo.
 * INSTRUCTIONS TO INSTALL:
 * -Create and publish samrtapp and Controller DTH in IDE.
 * -Open The App, click on "Discovery" to found TV.
 * -If Show 'Select LG TV(0 found)' click next until 'Select LG TV(1 found)', then 'Tap to set' to select IP TV"
 * -Select the TV app found, just find 1. click on "done", then key code appears on the TV screen, take note of it.
 * -(Every 15 seconds refresch TV found and clean the previous selection)
 * -Write an App Name to control One TV device. DTH created with same name.
 * -Click on "TV Key" and write the copied code TV pairing key.
 * -NOTE: Device controller works to Activate and Deactivate TV Power Save: screen ON-OFF and Mute OFF-ON. (TV 42" 12watt.consumption, TV 29" 9watt)
 * -Can turn TV Power Off with power button. When these LG TVs 2012 are power OFF, the network does not work and therefore it is impossible to turn them ON.
 * IMPORTANT: Must be configure in your router the Fix IP for your TV or you must write the TV IP in th device configuration even 12Hours normally

If you wish to add more commands, a full list of remote control commands are listed below

Virtual key code

(decimal number)

Description

1

POWER

2

Number 0

3

Number 1

4

Number 2

5

Number 3

6

Number 4

7

Number 5

8

Number 6

9

Number 7

10

Number 8

11

Number 9

12

UP key among remote Controller’s 4 direction keys

13

DOWN key among remote Controller’s 4 direction keys

14

LEFT key among remote Controller’s 4 direction keys

15

RIGHT key among remote Controller’s 4 direction keys

20

OK

21

Home menu

22

Menu key (same with Home menu key)

23

Previous key (Back)

24

Volume up

25

Volume down

26

Mute (toggle)

27

Channel UP (+)

28

Channel DOWN (-)

29

Blue key of data broadcast

30

Green key of data broadcast

31

Red key of data broadcast

32

Yellow key of data broadcast

33

Play

34

Pause

35

Stop

36

Fast forward (FF)

37

Rewind (REW)

38

Skip Forward

39

Skip Backward

40

Record

41

Recording list

42

Repeat

43

Live TV

44

EPG

45

Current program information

46

Aspect ratio

47

External input

48

PIP secondary video

49

Show / Change subtitle

50

Program list

51

Tele Text

52

Mark

400

3D Video

401

3D L/R

402

Dash (-)

403

Previous channel (Flash back)

404

Favorite channel

405

Quick menu

406

Text Option

407

Audio Description

408

NetCast key (same with Home menu)

409

Energy saving

410

A/V mode

411

SIMPLINK

412

Exit

413

Reservation programs list

414

PIP channel UP

415

PIP channel DOWN

416

Switching between primary/secondary video

417

My Apps

About
A Device Handler to connect 2012-2013 LG Smart TV's to Smart Things

Resources
 Readme
Releases
No releases published
Packages
No packages published
Languages
Groovy
