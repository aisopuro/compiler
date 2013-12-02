main {
  int a;
  a := 0;
  {
    print(a);
    a := a + 1;
  }
  while(a < 6)
  {
    print(a);
    a := a + 1;
  }

  return a;
}

