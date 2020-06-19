# Changelog

#### v0.2.3

 * Decoupled battery and cockpit from summary
 * Unified error model for all Kamereon calls
 * Error handling and display in "Status" and "Location" fragment
 * Fixed expired Gigya JWT on re-authentication

#### v0.2.2
 * Implemented sending charge command when plugged and not charging
 * Fixed proper HVAC command reporting (incl. command and temperature)

#### v0.2.1

 * Setting for pre-heating temperature target
 * Setting for notifications and thresholds (not used, though)

#### v0.2.0

 * Implemented HVAC start and cancel command
 * Added status text to plug and charge state
 * Implemented refresh for geo position
 * Reworked all network connectivity to RxJava observables

#### v0.1.6

 * Add data timestamp to status screen (+ layout adjustments)
 * Allow hiding VIN dropdown if only one vehicle
 * Do not place a dummy ZOE on map (fix two ZOEs on map)

#### v0.1.5

 * Fix selecting account from multiple acconts
 * Fix filtering vehicles to ZOEs only

#### v0.1.4

 * New feature: About screen with libraries
 * New feature: Settings screen
 * New feature: Use configurable distance and temperature units
 * New feature: Use configurable versions of API V1 and V2

#### v0.1.3

 * First alpha release for external testers

#### v0.1.2

 * Initial release (beta)

