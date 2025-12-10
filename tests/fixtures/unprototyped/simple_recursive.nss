void Recursive()
{
    if (GetIsObjectValid(OBJECT_SELF))
    {
        Recursive();
    }
}

void main()
{
    Recursive();
}

