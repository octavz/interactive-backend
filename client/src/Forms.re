open BsReactstrap;
open Format;

type validator = {
  isValid: string => bool,
  display: string => string,
};

module FormInput = {
  type state = {
    value: string,
    error: string,
  };

  [@react.component]
  let make =
      (
        ~id,
        ~label,
        ~onChange,
        ~onValidate=?,
        ~_type=?,
        ~placeholder=?,
        ~min=?,
        ~max=?,
        ~maxLength=?,
        ~value=?,
        ~validators=?,
      ) => {
    let _value = value |> Js.Option.getWithDefault("");
    let _placeholder = placeholder |> Js.Option.getWithDefault(label);
    let _validators = validators |> Js.Option.getWithDefault([||]);
    let _onValidate = onValidate |> Js.Option.getWithDefault(_ => ());

    let validate = text => {
      let valid = v => v.isValid(text) ? None : Some(v.display(label));
      _validators
      |> Array.map(valid)
      |> Js.Array.filter(Js.Option.isSome)
      |> Array.map(Js.Option.getWithDefault(""));
    };

    let (state, setState) = React.useState(() => {value: _value, error: ""});

    React.useEffect1(
      () => {
        let errors = validate(state.value);
        setState(_ => {...state, error: Js.Array.joinWith("", errors)});
        _onValidate(errors);
        None;
      },
      [|state.value|],
    );

    let handleOnChange = data => {
      setState(_ => {...state, value: data});
      onChange(data);
    };

    <div>
      <FormGroup row=true>
        <Label for_=id className="col-form-label text-right" md=2>
          {ReasonReact.string(sprintf("%s:", label))}
        </Label>
        <Col md=5>
          {switch (_type) {
           | Some("textarea") =>
             <textarea
               id
               placeholder=_placeholder
               className="form-control"
               maxLength={maxLength |> Js.Option.getWithDefault(10)}
               onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}
               value={state.value}
             />
           | optType =>
             <Input
               _type={optType |> Js.Option.getWithDefault("text")}
               id
               placeholder=_placeholder
               min={min |> Js.Option.getWithDefault(0.0)}
               max={max |> Js.Option.getWithDefault(100.0)}
               value={state.value}
               onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}
             />
           }}
        </Col>
      </FormGroup>
      <Row>
        <Col className={sprintf("offset-md-2 %s", String.length(state.error) == 0 ? "d-none" : "d-block")}>
          <Alert color="danger"> {React.string(state.error)} </Alert>
        </Col>
      </Row>
    </div>;
  };
};

module FormRadio = {
  [@react.component]
  let make = (~id, ~label, ~values: array((string, string)), ~value, ~onChange: string => unit) => {
    let firstValue = values |> Array.length == 0 ? "" : snd(values[0]);

    let (state, setState) = React.useState(() => firstValue);

    let handleOnChange = data => {
      setState(_ => data);
      onChange(data);
    };

    let renderRadio = ((label, _value): (string, string)) =>
      <div className="h-100 form-check form-check-inline" key={sprintf("key_%s", _value)}>
        <input
          name=id
          type_="radio"
          checked={state == _value}
          className="form-check-input"
          value=_value
          onChange={_ => handleOnChange(_value)}
        />
        <Label className="form-check-label" for_={id ++ value}>
          {React.string(sprintf("%s(%s)", label, _value))}
        </Label>
      </div>;

    <FormGroup row=true>
      <Label className="col-form-label text-right" md=2 for_=id> {React.string(label ++ ":")} </Label>
      <Col> {values |> Array.map(renderRadio) |> ReasonReact.array} </Col>
    </FormGroup>;
  };
};