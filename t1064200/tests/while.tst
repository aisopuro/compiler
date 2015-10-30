main begin
  int a;
  a <- 0;
  begin
    print(a);
    a <- a + 1;
  end
  while(a < 6)
  begin
    print(a);
    a <- a + 1;
  end

  return a;
end

