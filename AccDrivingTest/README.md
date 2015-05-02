<p align="center" >

Accurate Driving Test is a android application to evaluate a driver based on Washington State laws used at the DMV, while using an OBDII-elm327 device to monitor the car. The OBD device is used to pair with the car to evaluate speed, while using the GPD on the device to follow the car and place comments on a map used for further evaluation.

## How To Get Started

- [Download Accurate Driving Test ](ttps://github.com/rbergquist7/AccurateDrivingTest) Use the following as library projects in your workspace 

- [Download Event Bus] (https://github.com/greenrobot/EventBus) minor part of recording during an evaluation

- [Download Library](https://github.com/rbergquist7/Library) This is the main library file. The rest can be found in your SDK folder

- [Use Android-support-v7-appcompat] In the /sdk/extras/android/support/v7/ folder

- [Use google-play-services_lib] In the /sdk/extras/google/google_play_services/libproject/ folder

-[Use OBDII-elm327] device to be plugged into diagnostic port of a car, to sync over bluetooth. Connect to device using pair code 1234

## Communication

- If you **need help** , 
- If you **found a bug**, _and can provide steps to reliably reproduce it_ ,
- If you **have a feature request** ,
- If you **want to contribute**, [please email] (accuratedrivingtest@gmail.com) 

#### Podfile

```ruby
platform :Android, '21'
pod "Accurate Driving Test", "~> 1.0"
```

## Requirements

| Accurate driving test Version | Minimum Android Target  | Target Android Target| 
|:--------------------:|:---------------------------:|:---------------------------:|
|          2.x         |            SDK 11           |            SDK 21           |


## Architecture

### accuratedrivingtest

- `BeginEvaluation`
- `BluetoothIOGateway`
- `DeviceBroadcastReceiver`
- `Driver`
- `DuringEvaluation`
- `DuringEvaluationLoadTest`
- `GoogleMapsQuery`
- `LoginScreen`
- `MainActivity`
- `MainElmActivity`
- `MyLog`
- `PairedDevicesDialog`
- `ReviewEvaluation`
- `SecurePreferences`
- `SplashScreen`
- `User`
- `UserMenu`



### addroute

- `AddRoute`
- `AutoComplete`
- `CreateRoute`
- `CreateRouteMap`
- `EditRoute`
- `RouteState`

### testing

- `AddSwipeListener`
- `CreateTest`
- `JObject`
- `LoadTest`
- `TestDetailsGeneral`
- `TestingJSON`


### comments

- `CommentsFragment`
- `CommentTemplates`

## Usage

### login

Using the login screen, enter the corrent evaluators name and password. Using the login button, the software will check the password against that stored on the backend. If correct, the user may continue, else a message is displayed. The user may also hit continue to evaluate without an evaluator.

#### User Menu

The evaluator/user, after loging in or hitting continue, may choose one of several options from here. The following options (Add Route, Add Test, Begin Evaluation, During Evaluation, and Review Evaluation) are explained below.

#### Add Route

The evaluator/user made add 'waypoints' to create a new route. the waypoints are address points that are automaticly connected to draw a route to follow on the google maps' map
ex| Waypoint 1 : OR
        Waypoint 2 : WA

ex 2| Waypoint 1 : 14204 NE Salmon Creek Ave, Vancouver, WA 98686
      Waypoint 2 : 209 W McLoughlin Blvd, Vancouver, WA 98660

Once saved, the route will be added to a list, and be available for selection when begining an evaluation

#### Add Test

The different testing criteria based on the Washington State drivers manual is described in this section. The evaluator/user may select individual testing criteria that they would want to evaluate against, and build a test that can be added to a list, and selected when begining an evaluation.

#### BeginEvaluation

Once selected, the evaluator/user may select the desired route and test to use during an evaluation. A route must be selected. The evaluator/user must enter a drivers license number to be used to store on the server. The evaluators name is automaticly filled in based on the information entered when logging in. The user name is temporarily stored encrpytedly on a public file on the tablet and displayed on this page. 
Once the information fields are entered, th user may continue to during the evaluation

#### During Evaluation

The android tablet/phone will then load the selected route (drawn in blue) and begin by asking to connect to the OBD device. Once paired the tablet will be able to record information directly from the car.
After loading the route, the device will begin to use GPS to follow the driver around using GPS (latitude/longitude) coordinates. These are stored for later evaluation, while also drwan on the map (drawn in red)
After clicking the button to view OBD data, where the MPH and RPM is refreshed based on OBDII every time the GPS pulls a new Lat/long location

#### Review Evaluation

The user is allowed to enter a drivers license number, where to device connection to the server, and retrieves information stored about the first drive it can find that matches.
Information such as the evaluators name, Averge miles per hour, and comments are displayed in one section of the page
Other the other section of the page, the route chosen, route taken, and comments are displayed on a map. This enables the user to have a clear visual display of the route comparison and where the comments where placed based on point they happened

---

### Security Policy

When passing information between screens on the tablet, the strings are stored locally through the secure preferences class to encrpt the keys where the information is being stored.
When sending and recieving from the Fat Fractal server, the connection is based on a HTTPS server, a more secure form of HTTP

## Credits/Maintainers

Accurate Driving Test was originally created by [Ryan Bergquist](https://www.linkedin.com/in/ryanbergquist) and [Parker Kimbell](https://www.linkedin.com/in/parkerkimbell) 

## Contact

Email (accuratedrivingtest@gmail.com)

## License

Accurate Driving Test is available under the MIT license. See the LICENSE file for more info.
