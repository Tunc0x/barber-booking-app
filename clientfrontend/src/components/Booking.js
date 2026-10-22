import * as React from 'react';
import TextField from '@mui/material/TextField';
import { Paper, Button } from '@mui/material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import dayjs from 'dayjs';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import Grid from '@mui/material/Grid';
import '../Client.css';







export default function Booking({ activeBarber, appointments, setAppointments, showDeleteButton, setShowDeleteButton, isLoggedIn, loggedInAccount }) {

    const [name, setName] = React.useState(isLoggedIn ? loggedInAccount.name : '')
    const [email, setEmail] = React.useState(isLoggedIn ? loggedInAccount.email : '')
    const [age, setAge] = React.useState(isLoggedIn ? loggedInAccount.age : '')
    const [phoneNumber, setPhoneNumber] = React.useState(isLoggedIn ? loggedInAccount.phoneNumber : '')
    const [rawAppointmentDateTime, setRawAppointmentDateTime] = React.useState(dayjs())

    // Das ist der Client der ein Termin buchen will
    const [bookingClient, setBookingClient] = React.useState({})

    const paperStyleBooking = { padding: '50px 20px', width: 600, margin: "20px auto" }

    const isInputValid = () => !isInputBlank() && isEmailValid() && isAgeValid() && isPhoneNumberValid();


    const isInputBlank = () => (name === '' || email === '' || age === '' || phoneNumber === '');

    const isEmailValid = () => email.includes('@');

    const isAgeValid = () => /^\d+$/.test(age) && age > 5;

    const isPhoneNumberValid = () => /^\d+$/.test(phoneNumber);

    const handleBlur = (field) => {
        setTouchedFields(prevState => ({
            ...prevState,
            [field]: true
        }));
    };

    const isBlank = (value) => {
        return !value || value.trim() === '';
    }

    const [touchedFields, setTouchedFields] = React.useState({});


    const handleClick = (e) => {
        e.preventDefault()

        const dateTime = rawAppointmentDateTime.format('YYYY-MM-DDTHH:mm:ss')
        console.log(dateTime)
        const barber = activeBarber

        let clientToBook;


        if (isLoggedIn) {
          
            // Client wurde gespeichert. Nun können wir den Termin speichern.
            const appointment = { client: loggedInAccount, dateTime, barber };
            console.log(appointment);
            fetch("http://localhost:8080/appointments", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(appointment)
            }).then(() => {
                console.log("New appointment added");
                const newAppointments = [...appointments, appointment];
                setAppointments(newAppointments);
            });


        } else {
            const password = "";
            const client = { name, email, phoneNumber, age, password };
            console.log(client);
            fetch("http://localhost:8080/clients", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(client)
            }).then(res => res.json()).then(data => {
                console.log("New Client added with ID:", data.clientId);
                client.id = data.clientId;

                // Client wurde gespeichert. Nun können wir den Termin speichern.
                const appointment = { client: client, dateTime, barber };
                console.log(appointment);
                fetch("http://localhost:8080/appointments", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(appointment)
                }).then(() => {
                    console.log("New appointment added");
                    const newAppointments = [...appointments, appointment];
                    setAppointments(newAppointments);
                });
            });
        }

    }


    return (


        <Paper elevation={3} style={paperStyleBooking}>

            <CalendarMonthIcon fontSize='large' color='primary'></CalendarMonthIcon>

            <h3 style={{ color: "blue" }}><u>Book an Appointment</u></h3>


            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <TextField
                        id="outlined-basic"

                        label="Full Name"
                        variant="outlined"
                        fullWidth
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                        disabled={isLoggedIn}
                    />
                </Grid>

                <Grid item xs={12}>
                    <TextField onBlur={() => handleBlur("email")}
                        id="outlined-basic"
                        label="Email Address"
                        variant="outlined"
                        fullWidth
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        error={touchedFields.email && !isBlank(email) && !isEmailValid()}
                        helperText={touchedFields.email && !isBlank(email) && !isEmailValid() ? "Incorrect entry." : ""}
                        disabled={isLoggedIn}


                    />
                </Grid>
                <Grid item xs={12} >
                    <TextField id="outlined-basic" onBlur={() => handleBlur("age")}

                        label="Age"
                        variant="outlined"
                        fullWidth
                        value={age}
                        onChange={(e) => setAge(e.target.value)}
                        required
                        error={touchedFields.age && !isBlank(age) && !isAgeValid()}
                        helperText={touchedFields.age && !isBlank(age) && !isAgeValid() ? "Incorrect entry." : ""}
                        disabled={isLoggedIn}
                    />
                </Grid>

                <Grid item xs={12} >
                    <TextField onBlur={() => handleBlur("phoneNumber")}
                        id="outlined-basic"
                        label="Phone Number"
                        variant="outlined"
                        fullWidth
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        required
                        error={touchedFields.phoneNumber && !isBlank(phoneNumber) && !isPhoneNumberValid()}
                        helperText={touchedFields.phoneNumber && !isBlank(phoneNumber) && !isPhoneNumberValid() ? "Incorrect entry." : ""}
                        disabled={isLoggedIn}
                    />
                </Grid>
            </Grid>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '20px' }}>
                <DateTimePicker
                    label="Pick your date time"
                    value={rawAppointmentDateTime}
                    onChange={datetime => setRawAppointmentDateTime(datetime)}
                />

                <Button variant="contained" onClick={handleClick} disabled={!isInputValid()}>
                    Submit
                </Button>
            </div>

        </Paper>


    )
}