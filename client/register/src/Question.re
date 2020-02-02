open BsReactstrap;
open Forms;
open Models;

let required: Forms.validator = {
  isValid: x => String.length(x) > 0,
  display: Format.sprintf("%s should not be empty"),
};

type validationErrors = array(string);

type answer = {
  id: string, 
  content: string
}

type state = {
  id: string,
  content: string,
  qtype: string,
  currentAnswer: string,
  answers: array(answer),
  shouldPost: bool,
  errors: Js.Dict.t(validationErrors),
};

let emptyState = {
  id:"",
  content: "",
  qtype: "",
  currentAnswer: "",
  answers: [||],
  shouldPost: false,
  errors: Js.Dict.empty(),
};

type action =
  | CheckAnswer(string)
  | Post
  | PostFinished(Server.serverResponse(unit))
  | Validate(string, validationErrors);

let hasErrors = s => s.errors |> Js.Dict.values |> Js.Array.length > 0;

let reducer = (_state, action) => {
  switch (action) {
  | CheckAnswer(s) => {..._state, currentAnswer: s} 
  | Post => _state |> hasErrors ? _state : {..._state, shouldPost: true}
  | PostFinished(Ok(_)) => {..._state, shouldPost: false}
  | PostFinished(Error(_)) => {..._state, shouldPost: false}
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

  <Container>
    <Row>
      <Col md=4 className="text-center p-4 offset-4">
        <h3 className="text-center font-weight-bold text-nowrap">
          {ReasonReact.string("Wantsome")}
        </h3>
      </Col>
    </Row>
    <Row>
      <Col className="text-center p-4">
        <Button
          color="primary"
          size="lg"
          disabled={state.shouldPost || state |> hasErrors}
          onClick={_ => dispatch(Post)}>
          {React.string("Send")}
        </Button>
      </Col>
    </Row>
    <Modal isOpen={state.shouldPost} centered=true autoFocus=true>
      <ModalHeader> {React.string("Please Wait")} </ModalHeader>
      <ModalBody className="container">
        <Row>
          <Col md=1> <div className="spinner-border" /> </Col>
          <Col> <h3> {React.string("Loading.")} </h3> </Col>
        </Row>
      </ModalBody>
    </Modal>
  </Container>;
};
