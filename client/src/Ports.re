open BsReactstrap;

type portRec = {
  portIdx: int,
  portName: string,
  exposed: bool,
};
let emptyPort = {portIdx: 0, portName: "", exposed: false};

module PortsHeader = {
  type localState = {
    port: portRec,
    error: string,
  };

  let emptyState = {port: emptyPort, error: ""};

  type actionHeader =
    | Clear
    | Error(string)
    | PortNameChanged(string)
    | PortExposedChanged;

  [@react.component]
  let make = (~onAdd) => {
    let (state, dispatch) =
      React.useReducer(
        (_state, action) =>
          switch (action) {
          | PortNameChanged(data) => {
              error: "",
              port: {
                ..._state.port,
                portName: data,
              },
            }
          | PortExposedChanged => {
              error: "",
              port: {
                ..._state.port,
                exposed: !_state.port.exposed,
              },
            }
          | Clear => emptyState
          | Error(err) => {..._state, error: err}
          },
        emptyState,
      );

    let isValid = () =>
      !state.port.exposed || (String.length(state.port.portName) > 0);

    let handleClick = () =>
      if (isValid()) {
        onAdd(state.port);
        dispatch(Clear);
      } else {
        dispatch(Error("The port name should not be empty when exposed"));
      };

    <div>
      <FormGroup row=true>
        <Col>
          <Input
            name="portName"
            placeholder="Port Name"
            value={state.port.portName}
            onChange={ev =>
              dispatch(PortNameChanged(ReactEvent.Form.target(ev)##value))
            }
          />
        </Col>
        <Col className="form-check form-check-inline">
          <input
            className="form-check-input"
            type_="checkbox"
            placeholder="Port Exposed"
            checked={state.port.exposed}
            onChange={_ => dispatch(PortExposedChanged)}
          />
          <Label className="form-check-label">
            {React.string("Exposed")}
          </Label>
        </Col>
        <Col>
          <Button color="primary" onClick={_ => handleClick()}>
            {React.string("Add Port")}
          </Button>
        </Col>
      </FormGroup>
      <Row className={String.length(state.error) == 0  ? "d-none" : "d-block" }>
        <Col>
          <Alert color="danger"> {React.string({state.error})} </Alert>
        </Col>
      </Row>
    </div>;
  };
};

module PortsRepeater = {
  [@react.component]
  let make = (~ports: array(portRec), ~onDelete) => {
    let renderPort = p =>
      <tr key={Js.Int.toString(p.portIdx)}>
        <td> {React.string(Js.Int.toString(p.portIdx))} </td>
        <td> {React.string(p.portName)} </td>
        <td>
          <input
            type_="checkbox"
            checked={p.exposed}
            disabled=true
            className="form-control"
          />
        </td>
        <td>
          <Button onClick={_ => onDelete(p.portIdx)}>
            {React.string("Delete")}
          </Button>
        </td>
      </tr>;

    <tbody>
      {ports |> Array.map(renderPort) |> ReasonReact.array}
    </tbody>;
  };
};

type action =
  | AddPort(portRec)
  | DeletePort(int);

type state = {ports: array(portRec)};

let portsComparer = (x: portRec, y: portRec) =>
  Pervasives.compare(x.portIdx, y.portIdx);

let sortPorts = ports =>{
  ports |> Array.sort(portsComparer);
  ports;
};

[@react.component]
let make = (~label, ~value, ~onChange) => {
  let (state, dispatch) =
    React.useReducer(
      (_state, action) => {
        let newState =
          switch (action) {
          | AddPort(p) =>
            let newPort = {...p, portIdx: Array.length(_state.ports) + 1};
            {ports: Array.append([|newPort|], _state.ports) |> sortPorts};
          | DeletePort(idx) =>
            let sorted =
              _state.ports |> Js.Array.filter(p => p.portIdx != idx) |> sortPorts;
            let reindexed =
              sorted |> Js.Array.mapi((x, i) => {...x, portIdx: i + 1});
            {ports: reindexed};
          };
        onChange(newState.ports);
        newState;
      },
      {ports: value},
    );
  <div>
    <FormGroup row=true>
      <Label className="col-form-label text-right" md=2>
        {React.string(label ++ ":")}
      </Label>
      <Col md=8> <PortsHeader onAdd={s => dispatch(AddPort(s))} /> </Col>
    </FormGroup>
    <Row>
      <Col className="offset-md-2">
        <Table>
          <PortsRepeater
            ports={state.ports}
            onDelete={idx => dispatch(DeletePort(idx))}
          />
        </Table>
      </Col>
    </Row>
  </div>;
};