open Belt;

type dictString = Js.Dict.t(string);

module Date = {
    let encoder: Decco.encoder(Js.Date.t) =
      date => Js.Date.toISOString(date)->Decco.stringToJson;
    let decoder: Decco.decoder(Js.Date.t) =
      json => {
        switch (Decco.stringFromJson(json)) {
        | Result.Ok(v) => Js.Date.fromString(v)->Ok
        | Result.Error(_) as err => err
        };
      };
    let codec: Decco.codec(Js.Date.t) = (encoder, decoder);
    [@decco]
    type t = [@decco.codec codec] Js.Date.t;
};

[@decco]
type userDto = {
  id: option(string),
  firstName: string,
  lastName: string,
  birthday: [@decco.codec Date.codec] Js.Date.t,
  city: string,
  email: string,
  phone: string,
  occupation: int,
  fieldOfWork: int,
  englishLevel: int,
  itExperience: bool,
  experienceDescription: option(string),
  heardFrom: string,
};

[@decco.decoder]
type comboValueDto = {
  id: int,
  value: string,
  label: option(string),
};

[@decco.decoder]
type combosDto = {
  occupation:   array(comboValueDto),
  fieldOfWork:  array(comboValueDto),
  englishLevel: array(comboValueDto),
}

let emptyCombosDto = {
  occupation:   [||],
  fieldOfWork:  [||],
  englishLevel: [||],
}
