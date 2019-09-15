open BsReactstrap;
open Forms;
open Ports;
open Format;

let urlCreateCluster = "/api/clusters";

type cluster = {
  name: string,
  team: string,
  size: int,
  description: string,
  memorySizing: int,
  cpuSizing: string,
  ports: array(portRec),
};

type state = {
  cluster,
  shouldCreateCluster: bool,
  nameErrors: array(string),
  teamErrors: array(string),
};

let required: Forms.validator = {
  isValid: x => String.length(x) > 0,
  display: Format.sprintf("%s should not be empty"),
};

let testPorts =
  [|1, 2|]
  |> Array.map(x =>
       {portIdx: x, portName: sprintf("port-%i", x), exposed: x mod 2 == 0}
     );

let emptyState = {
  cluster: {
    name: "",
    team: "",
    size: 2,
    description: "",
    memorySizing: 1500,
    cpuSizing: "0.5",
    ports: testPorts,
  },
  shouldCreateCluster: false,
  teamErrors: [||],
  nameErrors: [||],
};

type httpResult('a) = Belt.Result.t(string, Js.Promise.error);

type action =
  | NameChanged(string)
  | TeamChanged(string)
  | SizeChanged(string)
  | DescriptionChanged(string)
  | MemoryChanged(int)
  | CpuChanged(string)
  | PortsChanged(array(portRec))
  | PostCluster
  | PostClusterFinished(httpResult(string))
  | ValidateName(array(string))
  | ValidateTeam(array(string));

[@react.component]
let make = () => {
  let hasErrors = s =>
    (s.nameErrors |> Js.Array.length) + (s.teamErrors |> Js.Array.length) > 0;

  let (state, dispatch) = {
    let reducer = (_state: state, action) => {
      switch (action) {
      | NameChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            name: data,
          },
        }
      | TeamChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            team: data,
          },
        }
      | SizeChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            size: int_of_string(data),
          },
        }
      | DescriptionChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            description: data,
          },
        }
      | MemoryChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            memorySizing: data,
          },
        }
      | CpuChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            cpuSizing: data,
          },
        }
      | PortsChanged(data) => {
          ..._state,
          cluster: {
            ..._state.cluster,
            ports: data,
          },
        }
      | PostCluster =>
        _state |> hasErrors ? _state : {..._state, shouldCreateCluster: true}
      | PostClusterFinished(Ok(_)) => {..._state, shouldCreateCluster: false}
      | PostClusterFinished(Error(_)) => {
          ..._state,
          shouldCreateCluster: false,
        }
      | ValidateName(e) => {..._state, nameErrors: e}
      | ValidateTeam(e) => {..._state, teamErrors: e}
      };
    };
    React.useReducer(reducer, emptyState);
  };

  let portToDto: portRec => Models.portDto =
    p => {
      name: p.portName,
      tags: p.exposed ? ["exposed_by_trusted_gateway"] : [],
    };

  let errHandler = e => {
    Js.log(e);
    dispatch(PostClusterFinished(Belt.Result.Error(e)))
    |> Js.Promise.resolve;
  };

  let wait = (f, e) =>
    Js.Global.setTimeout(() => f(e) |> ignore, 2000)
    |> ignore
    |> Js.Promise.resolve;

  let respHandler = r => {
    Js.log("Success");
    Js.log(r);
    dispatch(PostClusterFinished(Belt.Result.Ok(r |> Js.Json.stringify)))
    |> Js.Promise.resolve;
  };

  let createCluster = dto => {
    let payload = Models.clusterDto_encode(dto) |> Js.Json.stringify;
    Js.Promise.(
      Fetch.fetchWithInit(
        urlCreateCluster,
        Fetch.RequestInit.make(
          ~method_=Post,
          ~body=Fetch.BodyInit.make(payload),
          ~headers=
            Fetch.HeadersInit.make({"Content-Type": "application/json"}),
          (),
        ),
      )
      |> then_(Fetch.Response.json)
      |> then_(respHandler)
      |> catch(wait(errHandler))
      |> ignore
    );
  };

  let buildDto: unit => Models.clusterDto =
    () => {
      name: state.cluster.name,
      team: state.cluster.team,
      size: state.cluster.size,
      description: state.cluster.description,
      memorySizing: state.cluster.memorySizing,
      cpuSizing:
        try (Js.Float.fromString(state.cluster.cpuSizing)) {
        | _ => 1.125
        },
      ports: state.cluster.ports |> Array.map(portToDto),
      hosts: [||],
    };

  React.useEffect1(
    () => {
      Js.log(state);
      if (state.shouldCreateCluster) {
        Js.log("getting data");
        createCluster(buildDto());
      };
      None;
    },
    [|state.shouldCreateCluster|],
  );

  <div className="container">
    <Row>
      <Col className="text-center p-4 offset-2" md=4>
        <h3 className="font-weight-bold text-nowrap">
          {ReasonReact.string("New Cluster")}
        </h3>
      </Col>
    </Row>
    <Row>
      <Col>
        <Form>
          <FormInput
            id="name"
            label="Name"
            maxLength=10
            value={state.cluster.name}
            validators=[|required|]
            onValidate={e => dispatch(ValidateName(e))}
            onChange={v => dispatch(NameChanged(v))}
          />
          <FormInput
            id="team"
            label="Team"
            maxLength=10
            value={state.cluster.team}
            validators=[|required|]
            onValidate={e => dispatch(ValidateTeam(e))}
            onChange={v => dispatch(TeamChanged(v))}
          />
          <FormInput
            id="size"
            label="Size"
            _type="number"
            placeholder="Size"
            max=10.0
            value={Js.Int.toString(state.cluster.size)}
            onChange={v => dispatch(SizeChanged(v))}
          />
          <FormInput
            id="description"
            label="Description"
            _type="textarea"
            value={state.cluster.description}
            onChange={v => dispatch(DescriptionChanged(v))}
          />
          <FormRadio
            id="memory"
            label="Memory Sizing"
            value="1500"
            onChange={v => {
              let mem =
                try (int_of_string(v)) {
                | _ => 1500
                };
              dispatch(MemoryChanged(mem));
            }}
            values=[|
              ("Small", "1500"),
              ("Medium", "2000"),
              ("Large", "2500"),
              ("XLarge", "3000"),
            |]
          />
          <FormRadio
            id="cpu"
            label="CPU Sizing"
            value="0.125"
            onChange={newValue => dispatch(CpuChanged(newValue))}
            values=[|
              ("Small", "0.125"),
              ("Medium", "0.25"),
              ("Large", "0.5"),
              ("XLarge", "1"),
              ("XXLarge", "2"),
            |]
          />
          <Ports
            label="Ports"
            value={state.cluster.ports}
            onChange={ports => dispatch(PortsChanged(ports))}
          />
        </Form>
      </Col>
    </Row>
    <Row>
      <Col className="text-center p-4">
        <Button
          color="primary"
          size="lg"
          disabled={state.shouldCreateCluster || state |> hasErrors}
          onClick={_ => dispatch(PostCluster)}>
          {React.string("Send")}
        </Button>
      </Col>
    </Row>
    <Modal isOpen={state.shouldCreateCluster} centered=true autoFocus=true>
      <ModalHeader> {React.string("Please Wait")} </ModalHeader>
      <ModalBody className="container">
        <Row>
          <Col md=1> <div className="spinner-border" /> </Col>
          <Col> <h3> {React.string("Loading.")} </h3> </Col>
        </Row>
      </ModalBody>
    </Modal>
  </div>;
};