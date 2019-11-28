IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_sales_man]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_sales_man
END
go
create proc z_import_sales_man 
	@nsid varchar(20)  = '', 
	@firstName nvarchar(255) = null, 
	@middleName nvarchar(255) = null, 
	@lastName nvarchar(255) = null, 
	@NameDisplay nvarchar(255) = '', 
	@email nvarchar(255) = '', 
	@mobilePhone nvarchar(255) = ''
as
	
	if(@email is null) set @email = @NameDisplay + @nsid
	if @mobilePhone is not null
		begin
			set @mobilePhone = replace(@mobilePhone, '(','')
			set @mobilePhone = replace(@mobilePhone, ')','')
			set @mobilePhone = replace(@mobilePhone, '-','')
			set @mobilePhone = replace(@mobilePhone, ' ','')
		end
	declare @newid int
	select @newid = id from tbUser where UserEmail = @email
	if(@newid is null)
	begin
		insert into tbUser(nsid, UserEmail, Username, NameDisplay, Phone, FirstName, LastName) 
		values(@nsid, @email, @email, @NameDisplay, @mobilePhone,		  @firstName, @lastName)
		select @newid = @@IDENTITY
	end
	else
		update tbUser set nsid = @nsid 
			, firstName = @firstName, lastName = @lastName, Phone = @mobilePhone
		where id = @newid
	
go