# PlumbingScheduler
Allows a plumber to tap the button in the app when on a call to create and populate a calendar event.

## Intended Use Case
1. A plumber gets a call. The caller is a customer who wants to schedule a job. The plumber uses Google Calendar on his Android phone to manage his schedule.
2. While on the call, the plumber opens **PlumbingScheduler** and presses the button. This will create a new calendar event, populate it with relevant data, and open it for further editing. Data populated depends if the caller is already in the address book:
* If they are, the name of the event will be the caller's name and the location will be the caller's location.
* In any case, the notes field of the event will have the following text added: "Phone number: (person's phone number)". This will be tappable to directly call back the person later.

3. PlumbingScheduler will also work after a call has completed. It will remember the phone number of the last inbound call and autopopulate an event based on that number.

## Screenshots
![Home Screen](https://github.com/ephraimkunz/PlumbingScheduler/blob/master/PH_home.png)
![Event Modification Screen](https://github.com/ephraimkunz/PlumbingScheduler/blob/master/PH_event.png)
