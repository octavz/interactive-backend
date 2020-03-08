open Css;

let container =
  style([
    display(grid),
    gridTemplateColumns([fr(1.0), fr(1.0), fr(1.0)]),
    gridTemplateRows([auto]),
    gridTemplateAreas(
      `areas(["header header header", "label input .", ". button ."]),
    ),
  ]);
