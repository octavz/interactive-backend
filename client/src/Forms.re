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
        ~options=?,
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

    let renderOptions = () => {
      let renderOpt = ((v, l)) =>
        <option key=v value=v> {React.string(l)} </option>;
      options
      |> Js.Option.getWithDefault(Js.Dict.empty())
      |> Js.Dict.entries
      |> Array.map(renderOpt);
    };

    <FormGroup row=true>
      <Label for_=id className="col-form-label text-left">
        {ReasonReact.string(sprintf("%s:", label))}
      </Label>
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
       | Some("select") =>
         <Input
           _type="select"
           id
           invalid={String.length(state.error) > 0}
           placeholder=_placeholder
           value={state.value}
           onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}>
           {React.array(renderOptions())}
         </Input>
       | optType =>
         <Input
           _type={optType |> Js.Option.getWithDefault("text")}
           id
           invalid={String.length(state.error) > 0}
           placeholder=_placeholder
           min={min |> Js.Option.getWithDefault(0.0)}
           max={max |> Js.Option.getWithDefault(100.0)}
           value={state.value}
           onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}
         />
       }}
      <FormFeedback> {React.string(state.error)} </FormFeedback>
    </FormGroup>;
  };
};

module FormRadio = {
  [@react.component]
  let make =
      (
        ~id,
        ~label,
        ~values: array((string, string)),
        ~value,
        ~onChange: string => unit,
      ) => {
    let firstValue = values |> Array.length == 0 ? "" : snd(values[0]);

    let (state, setState) = React.useState(() => firstValue);

    let handleOnChange = data => {
      setState(_ => data);
      onChange(data);
    };

    let renderRadio = ((label, _value): (string, string)) =>
      <FormGroup check=true key={sprintf("key_%s", _value)}>
        <Label check=true className="form-check-label" for_={id ++ value}>
          <input
            name=id
            type_="radio"
            checked={state == _value}
            className="form-check-input"
            value=_value
            onChange={_ => handleOnChange(_value)}
          />
          {React.string(sprintf(" %s", label))}
        </Label>
      </FormGroup>;

    <FormGroup tag=id row=true>
      <div className="d-flex flex-column">
        <div> {React.string(label ++ ":")} </div>
        <div className="d-flex flex-column">
          {values |> Array.map(renderRadio) |> ReasonReact.array}
        </div>
      </div>
    </FormGroup>;
  };
};
