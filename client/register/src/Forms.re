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
        /* ~min=?, */
        /* ~max=?, */
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

    <>
        <label htmlFor=id>
          {ReasonReact.string(sprintf("%s:", label))}
        </label>
        {switch (_type) {
         | Some("textarea") =>
           <textarea
             className="input"
             id
             placeholder=_placeholder
             maxLength={maxLength |> Js.Option.getWithDefault(10)}
             onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}
             value={state.value}
           />
         | Some("select") =>
           <select
             id
             className="input"
             //invalid={String.length(state.error) > 0}
             placeholder=_placeholder
             value={state.value}
             onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}>
             {React.array(renderOptions())}
           </select>
         | optType =>
           <input
             id
             name=id
             className="input"
             type_={optType |> Js.Option.getWithDefault("text")}
             //invalid={String.length(state.error) > 0}
             placeholder=_placeholder
             /* min={min |> Js.Option.getWithDefault(0.0)} */
             /* max={max |> Js.Option.getWithDefault(100.0)} */
             value={state.value}
             onChange={e => handleOnChange(ReactEvent.Form.target(e)##value)}
           />
         }}
        <span className="error"> {React.string(state.error)} </span>
    </>;
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
      <div key={sprintf("key_%s", _value)}>
        <label>
          <input
            name=id
            type_="radio"
            checked={state == _value}
            className="form-check-input"
            value=_value
            onChange={_ => handleOnChange(_value)}
          />
          {React.string(sprintf(" %s", label))}
        </label>
      </div>;

    <>
      <label className="label"> {React.string(label ++ ":")} </label>
      <div className="input">
        {values |> Array.map(renderRadio) |> ReasonReact.array}
      </div>
    </>;
  };
};
