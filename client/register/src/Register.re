open Forms;
open Models;

let required: Forms.validator = {
  isValid: x => String.length(x) > 0,
  display: Format.sprintf("%s should not be empty"),
};

type validationErrors = array(string);

type state = {
  user: userDto,
  shouldInit: bool,
  shouldPost: bool,
  combos: combosDto,
  errors: Js.Dict.t(validationErrors),
};

let emptyState = {
  user: {
    id: None,
    firstName: "",
    lastName: "",
    birthday: Js.Date.fromString("1980-01-01T00:00:00Z"),
    city: "Iasi",
    email: "",
    phone: "",
    occupation: 0,
    fieldOfWork: 0,
    englishLevel: 0,
    itExperience: false,
    experienceDescription: None,
    heardFrom: "",
  },
  combos: emptyCombosDto,
  shouldPost: false,
  shouldInit: true,
  errors: Js.Dict.empty(),
};

type action =
  | FirstNameChanged(string)
  | LastNameChanged(string)
  | BirthdayChanged(Js.Date.t)
  | CityChanged(string)
  | EmailChanged(string)
  | PhoneChanged(string)
  | OccupationChanged(int)
  | FieldOfWorkChanged(int)
  | EnglishLevelChanged(int)
  | ITExpChanged(bool)
  | ExpDescChanged(string)
  | HeardFromChanged(string)
  | Post
  | PostFinished(Server.serverResponse(unit))
  | RequestCombos(Server.serverResponse(combosDto))
  | Validate(string, validationErrors);

let hasErrors = s => s.errors |> Js.Dict.values |> Js.Array.length > 0;

let reducer = (_state, action) => {
  let update = u => {
    Js.log(u);
    {..._state, user: u};
  };
  switch (action) {
  | FirstNameChanged(s) => {..._state.user, firstName: s} |> update
  | LastNameChanged(s) => {..._state.user, lastName: s} |> update
  | BirthdayChanged(s) => {..._state.user, birthday: s} |> update
  | CityChanged(s) => {..._state.user, city: s} |> update
  | EmailChanged(s) => {..._state.user, email: s} |> update
  | PhoneChanged(s) => {..._state.user, phone: s} |> update
  | OccupationChanged(i) => {..._state.user, occupation: i} |> update
  | FieldOfWorkChanged(i) => {..._state.user, fieldOfWork: i} |> update
  | EnglishLevelChanged(i) => {..._state.user, englishLevel: i} |> update
  | ITExpChanged(b) => {..._state.user, itExperience: b} |> update
  | ExpDescChanged(s) =>
    {..._state.user, experienceDescription: Some(s)} |> update
  | HeardFromChanged(s) => {..._state.user, heardFrom: s} |> update
  | Post => _state |> hasErrors ? _state : {..._state, shouldPost: true}
  | PostFinished(Ok(_)) => {..._state, shouldPost: false}
  | PostFinished(Error(_)) => {..._state, shouldPost: false}
  | RequestCombos(Ok(combos)) => {..._state, combos, shouldInit: false}
  | RequestCombos(Error(_)) => {..._state, shouldInit: false}
  | Validate(f, e) =>
    Js.Dict.set(_state.errors, f, e);
    _state;
  };
};

let dateToString = d =>
  d
  |> Js.Date.toISOString
  |> Js.String.split("T")
  |> Js.Array.unsafe_get(_, 0);

[@react.component]
let make = () => {
  let (state, dispatch) = React.useReducer(reducer, emptyState);

  /*
   let errHandler = e => {
     dispatch(PostFinished(Belt.Result.Error(e))) |> Js.Promise.resolve;
   };

   let respHandler = r => {
     dispatch(PostFinished(Belt.Result.Ok())) |> Js.Promise.resolve;
   };

      React.useEffect1(
        () => {
          if (state.shouldPost) {
            Server.postUser(state.user, respHandler, errHandler);
          };
          None;
        },
        [|state.shouldPost|],
      );
    */

  let errHandlerInit = e => {
    dispatch(RequestCombos(Belt.Result.Error(e))) |> Js.Promise.resolve;
  };

  let respHandlerInit = r => {
    dispatch(RequestCombos(Belt.Result.Ok(r))) |> Js.Promise.resolve;
  };

  React.useEffect1(
    () => {
      if (state.shouldInit) {
        Server.getCombos(respHandlerInit, errHandlerInit);
      };
      None;
    },
    [|state.shouldPost|],
  );

  <form>
     <h2> {ReasonReact.string("Wantsome")} </h2> 
      <FormInput
        id="firstName"
        label="Prenume"
        value={state.user.firstName}
        validators=[|required|]
        onValidate={e => dispatch(Validate("firstName", e))}
        onChange={v => dispatch(FirstNameChanged(v))}
      />
      <FormInput
        id="lastName"
        label="Nume"
        value={state.user.lastName}
        validators=[|required|]
        onValidate={e => dispatch(Validate("lastName", e))}
        onChange={v => dispatch(LastNameChanged(v))}
      />
      <FormInput
        id="birthday"
        label="Data nasterii"
        _type="date"
        value={state.user.birthday |> dateToString}
        validators=[|required|]
        onValidate={e => dispatch(Validate("birthday", e))}
        onChange={v => dispatch(BirthdayChanged(Js.Date.fromString(v)))}
      />
      <FormInput
        id="email"
        label="Email"
        value={state.user.email}
        validators=[|required|]
        onValidate={e => dispatch(Validate("email", e))}
        onChange={v => dispatch(EmailChanged(v))}
      />
      <FormInput
        id="occupation"
        _type="select"
        label="Momentan sunt"
        value={string_of_int(state.user.occupation)}
        onChange={v => dispatch(OccupationChanged(int_of_string(v)))}
        options={Js.Dict.fromArray(
          state.combos.occupation
          |> Js.Array.map(v =>
               (
                 v.id |> string_of_int,
                 v.label |> Js.Option.getWithDefault(""),
               )
             ),
        )}
      />
      <FormInput
        id="fieldOfWork"
        _type="select"
        label="Domeniu de activitate"
        value={string_of_int(state.user.fieldOfWork)}
        onChange={v => dispatch(FieldOfWorkChanged(int_of_string(v)))}
        options={Js.Dict.fromArray(
          state.combos.fieldOfWork
          |> Js.Array.map(v =>
               (
                 v.id |> string_of_int,
                 v.label |> Js.Option.getWithDefault(""),
               )
             ),
        )}
      />
      <FormInput
        id="englishLevel"
        _type="select"
        label="Nivelul limbii engleze"
        value={string_of_int(state.user.englishLevel)}
        onChange={v => dispatch(EnglishLevelChanged(int_of_string(v)))}
        options={Js.Dict.fromArray(
          state.combos.englishLevel
          |> Js.Array.map(v =>
               (
                 v.id |> string_of_int,
                 v.label |> Js.Option.getWithDefault(""),
               )
             ),
        )}
      />
      <FormRadio
        id="itExperience"
        label="Cunostinte in domeniul IT"
        value={string_of_bool(state.user.itExperience)}
        onChange={v => dispatch(ITExpChanged(bool_of_string(v)))}
        values=[|("Da", "true"), ("Nu", "false")|]
      />
      <FormInput
        id="expDescription"
        label={js|Dacă ai răspuns da mai sus, te rugăm să detaliezi|js}
        placeholder=""
        _type="textarea"
        value={state.user.email}
        onChange={v => dispatch(ExpDescChanged(v))}
      />
    
    <button
      className="button"
      disabled={state.shouldPost || state |> hasErrors}
      onClick={_ => dispatch(Post)}>
      {React.string("Send")}
    </button>
    /* <Modal isOpen={state.shouldPost} centered=true autoFocus=true> */
    /*   <ModalHeader> {React.string("Please Wait")} </ModalHeader> */
    /*   <ModalBody className="container"> */
    /*     <Row> */
    /*       <Col md=1> <div className="spinner-border" /> </Col> */
    /*       <Col> <h3> {React.string("Loading.")} </h3> </Col> */
    /*     </Row> */
    /*   </ModalBody> */
    /* </Modal> */
  </form>;
};
