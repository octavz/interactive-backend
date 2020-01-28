open Models;

let baseUrl = "http://localhost:5000";
let urlPostUser = Format.sprintf("%s/api/user", baseUrl);

let restHeaders =
  Fetch.HeadersInit.makeWithArray([|
    ("Content-Type", "application/json"),
    ("Accept", "application/json"),
  |]);

type serverError =
  | ResponseError(Js.Promise.error)
  | SerdeError(exn);

type serverResponse('a) = Belt.Result.t('a, serverError);

let fromJson = (decoder, cbOk, cbErr, json) => {
  switch (decoder(json)) {
  | result => cbOk(result)
  | exception e => cbErr(SerdeError(e))
  };
};

let postUser = (dto, cbOk, cbErr) => {
  let payload = userDto_encode(dto) |> Js.Json.stringify;
  Js.Promise.(
    Fetch.fetchWithInit(
      urlPostUser,
      Fetch.RequestInit.make(
        ~method_=Post,
        ~body=Fetch.BodyInit.make(payload),
        ~headers=restHeaders,
        (),
      ),
    )
    |> then_(Fetch.Response.json)
    |> then_(fromJson(userDto_decode, cbOk, cbErr, _))
    |> catch(e => cbErr(ResponseError(e)))
    |> ignore
  );
};

let getCombos = (cbOK, cbErr) => {
  let cmbOccupation = [|
    {id: 1, value: "Employee", label: Some("Employee")},
    {id: 2, value: "Student", label: Some("Student")},
    {id: 3, value: "SelfEmployed", label: Some("Self Employee")},
    {id: 4, value: "No Occupation", label: Some("No Occupation")},
  |];
  let cmbFieldOfWork = [|
    {
      id: 1,
      value: "Business/Management",
      label: Some("Business/Management"),
    },
    {
      id: 2,
      value: "Customer Support/Call Center",
      label: Some("Customer Support/Call Center"),
    },
    {id: 3, value: "Law", label: Some("Law")},
    {id: 4, value: "Education/Training", label: Some("Education/Training")},
    {id: 5, value: "Finance/Banks", label: Some("Finance/Banks")},
    {id: 6, value: "HR/Human Resources", label: Some("HR/Human Resources")},
    {
      id: 7,
      value: "It/Engineering/Technical",
      label: Some("It/Engineering/Technical"),
    },
  |];
  let cmbEnglishLevel = [|
    {id: 1, value: "Beginner", label: Some("Beginner")},
    {id: 2, value: "Average", label: Some("Average")},
    {id: 2, value: "Advanced", label: Some("Advanced")},
  |];
  let combosDTO = {
    occupation: cmbOccupation,
    fieldOfWork: cmbFieldOfWork,
    englishLevel: cmbEnglishLevel,
  };
  Js.Promise.(
    make((~resolve, ~reject) => resolve(. combosDTO))
    |> then_(r => cbOK(r))
    |> catch(e => cbErr(ResponseError(e)))
    |> ignore
  );
};

open Belt;

module type IO = {
  type env;
  type error;

  type cause('e) =
    | Empty
    | Die(exn)
    | Error('e);

  type t(_) =
    | Effect(unit => 'any): t(env, error, 'any)
    | Bind(t(env, error, 'any), 'any => t(env, error, 'b)): t(env, error, 'b)
    | Succeed('any): t(env, error, 'any)
    | Fail(cause(error)): t(env, error, 'any)
    | Done(Result.t('any, 'e)): t(env, error, 'any);

  type stackEntry('any) = 'any => t(env, error, 'any);
};

module ZIO = (I: IO) => {

  /* let nextInstr: (MutableStack.t(IO.stackEntry('any)),'any) => t(IO.env, IO.error, 'any); */
  /* let run: t(IO.env, IO.error, 'any) => unit;  */
  /* let nextInstr = value => { */
  /*     switch(stack |> MutableStack.pop) { */
  /*       | Some(k) => k(value) */
  /*       | _ => Done(Ok(value)) */
  /*       }; */
  /* }; */
  /*  */
  /*  */
  let rec run: 'any. I.t('a) => Result('any, I.error) =
    zio => {
        switch (zio) {
        | Effect(f) =>
          switch (f()) {
          | res => currZio := Succeed(res)
          | exception e =>
            Js.log(
              Format.sprintf("Unhandled error: %s", e |> Js.String.make),
            );
            currZio := Fail(Die(e));
          }
        | Bind(z, k) =>

          switch (run(z)) {
          | Ok(va) => currZio ;= k(va)
          | Error(e) => Error(e)
          }
        | Succeed(v) => Ok(v)
        | Fail(c) =>
          switch (c) {
          | Die(ex) => raise(ex)
          | Error(e) => raise(e)
          }
        };
  };
};
