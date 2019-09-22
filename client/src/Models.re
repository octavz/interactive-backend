type dictString = Js.Dict.t(string);

type userDto = {
  id: option(string),
  firstName: string,
  lastName: string,
  birthday: Js.Date.t,
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

type comboItemDto = {
  id: int,
  value: string,
  label: option(string),
};

module Decode = {
  open! Json.Decode;

  let userDtoDecode = json => {
    id: json |> field("id", optional(string)),
    firstName: json |> field("firstName", string),
    lastName: json |> field("lastName", string),
    birthday: json |> field("birthday", date),
    city: json |> field("city", string),
    email: json |> field("email", string),
    phone: json |> field("phone", string),
    occupation: json |> field("occupation", int),
    fieldOfWork: json |> field("fieldOfWork", int),
    englishLevel: json |> field("englishLevel", int),
    itExperience: json |> field("itExperience", bool),
    experienceDescription:
      json |> field("experienceDescription", optional(string)),
    heardFrom: json |> field("heardFrom", string),
  };

  let comboItemDtoDecode = json => {
    id: json |> field("id", int),
    value: json |> field("value", string),
    label: json |> field("label", optional(string)),
  };
};

module Encode = {
  let userDtoEncode = (c: userDto) => {
    open! Json.Encode;
    object_([
      ("id", c.id |> nullable(string)),
      ("firstName", string(c.firstName)),
      ("lastName", string(c.lastName)),
      ("birthday", date(c.birthday)),
      ("city", string(c.city)),
      ("email", string(c.email)),
      ("phone", string(c.phone)),
      ("occupation", int(c.occupation)),
      ("fieldOfWork", int(c.fieldOfWork)),
      ("englishLevel", int(c.englishLevel)),
      ("itExperience", bool(c.itExperience)),
      ("experienceDescription", c.experienceDescription |> nullable(string)),
      ("heardFrom", string(c.heardFrom)),
    ]);
  };
};