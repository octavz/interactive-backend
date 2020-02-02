type route =
  | Register;

type state = {currentPage: route};

let labels = Js.Dict.fromArray([|(Js.String.make(Register), "Register")|]);

let label = route =>
  route
  |> Js.String.make
  |> Js.Dict.get(labels)
  |> Js.Option.getWithDefault("");

let emptyState = {currentPage: Register};

[@react.component]
let make = () => {
  let url = ReasonReactRouter.useUrl();
  let (state, setState) = React.useState(() => emptyState);

  <div>
    {switch (url.path) {
     | _ =>
       if (state.currentPage != Register) {
         setState(_ => {currentPage: Register});
       };
       <Register/>;
     }}
  </div>;
};
