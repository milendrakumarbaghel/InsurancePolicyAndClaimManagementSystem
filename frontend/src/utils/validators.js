// Validation rules mirror the @Valid constraints on the Spring Boot DTOs
// so the person sees the same rules on the client before hitting the API.

export const patterns = {
  fullName: /^[a-zA-Z\s]+$/,
  mobileNumber: /^[6-9]\d{9}$/,
  password: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/,
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  otp6: /^\d{6}$/,
  noAngleBrackets: /^[^<>]*$/,
  lettersSpaces: /^[a-zA-Z\s]+$/,
  pinCode: /^[1-9][0-9]{5}$/,
  policyNumber: /^[A-Z0-9-]+$/,
  upperUnderscore: /^[A-Z_]+$/,
  documentName: /^[a-zA-Z0-9\s_()\.-]+$/,
  documentType: /^[a-zA-Z\s/-]+$/,
  planName: /^[a-zA-Z0-9\s'(),.-]+$/,
};

export const required =
  (message = "This field is required") =>
  (value) => {
    if (value === null || value === undefined || value === "") return message;
    if (Array.isArray(value) && value.length === 0) return message;
    return null;
  };

export const minLength = (min, message) => (value) => {
  if (value && value.length < min)
    return message || `Must be at least ${min} characters`;
  return null;
};

export const maxLength = (max, message) => (value) => {
  if (value && value.length > max)
    return message || `Must be at most ${max} characters`;
  return null;
};

export const pattern = (regex, message) => (value) => {
  if (value && !regex.test(value)) return message;
  return null;
};

export const email =
  (message = "Invalid email format") =>
  (value) => {
    if (value && !patterns.email.test(value)) return message;
    return null;
  };

export const min = (m, message) => (value) => {
  if (
    value !== "" &&
    value !== null &&
    value !== undefined &&
    Number(value) < m
  ) {
    return message || `Must be at least ${m}`;
  }
  return null;
};

export const max = (m, message) => (value) => {
  if (
    value !== "" &&
    value !== null &&
    value !== undefined &&
    Number(value) > m
  ) {
    return message || `Must be at most ${m}`;
  }
  return null;
};

export const positive =
  (message = "Must be greater than zero") =>
  (value) => {
    if (
      value !== "" &&
      value !== null &&
      value !== undefined &&
      Number(value) <= 0
    )
      return message;
    return null;
  };

export const matchesField = (otherValue, message) => (value) => {
  if (value !== otherValue) return message;
  return null;
};

export const pastOrPresent =
  (message = "Cannot be a future date") =>
  (value) => {
    if (!value) return null;
    const d = new Date(value);
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    if (d > today) return message;
    return null;
  };

export const futureOrPresent =
  (message = "Cannot be a past date") =>
  (value) => {
    if (!value) return null;
    const d = new Date(value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (d < today) return message;
    return null;
  };

export const pastDate18Years =
  (message = "You must be at least 18 years old") =>
  (value) => {
    if (!value) return null;
    const dob = new Date(value);
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    if (dob > today) return "Date of birth cannot be in the future";
    const cutoff = new Date();
    cutoff.setFullYear(cutoff.getFullYear() - 18);
    if (dob > cutoff) return message;
    return null;
  };

/**
 * Runs an array of validator functions against a value, returning the first error found.
 */
export function runValidators(value, validators = []) {
  for (const validator of validators) {
    const error = validator(value);
    if (error) return error;
  }
  return null;
}

/**
 * Validates a whole form object against a schema: { field: [validatorFns] }
 * Returns an { field: errorMessage } map containing only fields with errors.
 */
export function validateForm(values, schema) {
  const errors = {};
  for (const field of Object.keys(schema)) {
    const error = runValidators(values[field], schema[field]);
    if (error) errors[field] = error;
  }
  return errors;
}
