main {
  boolean a;
  boolean b;
  a := true;
  b := false;
  if (a && b) then
  {
    print(1);
  }
  else
  {
    print(0);
  }
  fi
  
  if (((1 + 3) < (4 + 5)) && ((2 < 4) && (6 < 1))) then {
  	a := b;
  }
  else {
  	b := a;
  }
  fi
  
  return a;
}

