
[@decco] type portDto = {
  name:string,
  tags:list(string),
};

[@decco] type clusterDto = {
  name: string,
  description: string,
  size: int,
  cpuSizing: float,
  memorySizing: int,
  team: string,
  ports: array(portDto),
  hosts: array(string)
}; 
