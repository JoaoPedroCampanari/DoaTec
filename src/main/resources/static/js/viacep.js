/**
 * ViaCEP auto-fill utility
 * Usage: initViaCEP(cepInput, { logradouro, bairro, cidade, estado }, options?)
 *   cepInput: the CEP input element (should have IMask applied)
 *   fields: map of field names to their input elements
 *   options.prefix: if field IDs have a prefix (e.g. 'profile' for 'profileLogradouro')
 *   options.cepMask: IMask instance for the CEP input (used for unmaskedValue)
 *   options.onSuccess: callback after successful CEP lookup
 */
function initViaCEP(cepInput, fields, options = {}) {
    const { cepMask, onSuccess } = options;

    cepInput.addEventListener('blur', async function () {
        const cep = cepMask ? cepMask.unmaskedValue : cepInput.value.replace(/\D/g, '');

        if (cep.length !== 8) {
            clearFields(fields);
            unlockFields(fields);
            return;
        }

        this.classList.add('viacep-loading');

        const result = await lookupCEP(cep);

        this.classList.remove('viacep-loading');

        if (result.erro) {
            DoaTec.showToast(result.erro, 'warning');
            clearFields(fields);
            unlockFields(fields);
        } else {
            if (fields.logradouro) fields.logradouro.value = result.logradouro;
            if (fields.bairro) fields.bairro.value = result.bairro;
            if (fields.cidade) fields.cidade.value = result.cidade;
            if (fields.estado) fields.estado.value = result.estado;

            lockFields(fields);

            if (result.logradouro && fields.numero) {
                fields.numero.focus();
            }

            if (onSuccess) onSuccess(result);
        }
    });
}

async function lookupCEP(cep) {
    try {
        const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
        const data = await response.json();

        if (data.erro) {
            return { erro: 'CEP não encontrado' };
        }

        return {
            logradouro: data.logradouro || '',
            bairro: data.bairro || '',
            cidade: data.localidade || '',
            estado: data.uf || ''
        };
    } catch (error) {
        return { erro: 'Erro ao buscar CEP. Verifique sua conexão.' };
    }
}

function clearFields(fields) {
    Object.values(fields).forEach(el => {
        if (el) el.value = '';
    });
}

function unlockFields(fields) {
    [fields.logradouro, fields.bairro, fields.cidade, fields.estado].forEach(el => {
        if (el) {
            el.removeAttribute('readonly');
            el.classList.remove('viacep-auto');
        }
    });
}

function lockFields(fields) {
    [fields.logradouro, fields.bairro, fields.cidade, fields.estado].forEach(el => {
        if (el) {
            el.setAttribute('readonly', 'readonly');
            el.classList.add('viacep-auto');
        }
    });
}

window.DoaTec = window.DoaTec || {};
window.DoaTec.initViaCEP = initViaCEP;
