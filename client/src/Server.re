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
  let payload = Encode.userDtoEncode(dto) |> Js.Json.stringify;
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
    |> then_(fromJson(Decode.userDtoDecode, cbOk, cbErr, _))
    |> catch(e => cbErr(ResponseError(e)))
    |> ignore
  );
};