BEGIN
    IF (COL_LENGTH('[dbo].[tbOrder]', 'nsid') IS NULL)
    BEGIN
        alter table tbOrder add nsid nvarchar(20)
    END
END;
BEGIN
    IF (COL_LENGTH('[dbo].[tbOrderDetail]', 'nsid') IS NULL)
    BEGIN
        alter table tbOrderDetail add nsid nvarchar(20)
    END
END;

BEGIN
    IF (COL_LENGTH('[dbo].[tbCustomer]', 'nsid') IS NULL)
    BEGIN
        alter table tbCustomer add nsid nvarchar(20)
    END
END;

BEGIN
    IF (COL_LENGTH('[dbo].[tbMemo]', 'nsid') IS NULL)
    BEGIN
        alter table tbMemo add nsid nvarchar(20)
    END
END;
BEGIN
    IF (COL_LENGTH('[dbo].[tbUser]', 'nsid') IS NULL)
    BEGIN
        alter table tbUser add nsid nvarchar(20)
    END
END;
go
drop table z_import_result
go

create table z_import_result(
	SONumber varchar(50),
	OrderId int,
	note nvarchar(max));
create index SONumber_idx on z_import_result(SONumber)
go