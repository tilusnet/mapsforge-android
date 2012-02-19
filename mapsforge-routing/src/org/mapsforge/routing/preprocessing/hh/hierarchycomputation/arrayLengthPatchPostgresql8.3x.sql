/* POSTGRES 8.3.x WORKAROUND FOR THE MISSING FUCNTION 'array_length' */

CREATE OR REPLACE FUNCTION array_length(arr DOUBLE PRECISION[], dimension INTEGER)
  RETURNS INTEGER AS $$
DECLARE
	ret INTEGER;
	ad TEXT;
BEGIN
	SELECT INTO ad array_dims(arr);
	SELECT INTO ret substr( ad , strpos(ad,':') + 1 , char_length(ad) - strpos(ad,':') - 1)::integer;
	RETURN ret;
END;
$$ LANGUAGE 'plpgsql';
